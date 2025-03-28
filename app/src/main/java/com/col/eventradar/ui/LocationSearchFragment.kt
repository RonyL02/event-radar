package com.col.eventradar.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.R
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentSearchBinding
import com.col.eventradar.models.common.AreaOfInterest
import com.col.eventradar.models.common.User
import com.col.eventradar.ui.adapters.LocationSearchResultsAdapter
import com.col.eventradar.ui.components.GpsLocationSearchFragment
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.ui.viewmodels.AreasViewModel
import com.col.eventradar.ui.viewmodels.AreasViewModelFactory
import com.col.eventradar.ui.viewmodels.LocationSearchViewModel
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory
import com.col.eventradar.ui.views.NoLastDividerItemDecoration
import com.col.eventradar.utils.KeyboardUtils
import com.col.eventradar.utils.UserAreaManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LocationSearchFragment : Fragment() {
    private var bindingInternal: FragmentSearchBinding? = null
    private val binding get() = bindingInternal!!
    private val viewModel: LocationSearchViewModel by viewModels()

    private var listener: MapFragmentListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isResultChosen = false
    private var gpsFragment: GpsLocationSearchFragment? = null
    private var toastFragment: ToastFragment = ToastFragment()
    private var currentUser: User? = null

    private val areasViewModel: AreasViewModel by activityViewModels {
        val repository = AreasOfInterestRepository(requireContext())
        AreasViewModelFactory(repository)
    }

    private val userViewModel: UserViewModel by activityViewModels {
        val userRepository = UserRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        val areasRepository = AreasOfInterestRepository(requireContext())
        UserViewModelFactory(commentRepository, userRepository, areasRepository)
    }

    private val searchResultsAdapter =
        LocationSearchResultsAdapter(
            onClick = ::onSearchResultClick,
            onRemove = ::onSearchResultRemove,
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentSearchBinding.inflate(inflater, container, false)
        gpsFragment = GpsLocationSearchFragment()

        childFragmentManager
            .beginTransaction()
            .add(R.id.gpsContainer, gpsFragment!!, GpsLocationSearchFragment.TAG)
            .commit()

        binding.apply {
            with(searchResultsRecyclerView) {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = searchResultsAdapter
                val divider =
                    NoLastDividerItemDecoration(
                        requireContext(),
                        RecyclerView.VERTICAL,
                        R.drawable.event_list_divider,
                    )
                searchResultsRecyclerView.addItemDecoration(divider)
            }
            searchEditText.setOnFocusChangeListener { _, hasFocus ->
                gpsFragment?.onFocusChange(hasFocus)
                if (hasFocus && searchResultsAdapter.itemCount > 0) {
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                }
            }

            searchEditText.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(searchValue: Editable?) {
                        if (isResultChosen) {
                            isResultChosen = false
                            return
                        }

                        searchRunnable?.let {
                            handler.removeCallbacks(it)
                        }

                        searchRunnable =
                            Runnable {
                                val query = searchValue.toString()
                                if (query.isNotEmpty()) {
                                    viewModel.searchCountryLocations(query)
                                }
                            }
                        searchRunnable?.let {
                            val debounceDelayMillis = 500L
                            handler.postDelayed(it, debounceDelayMillis)
                        }
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                    }
                },
            )
        }

        observeViewModel()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? MapFragmentListener
            ?: throw RuntimeException("$parentFragment must implement MapFragmentListener")
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.user.collect { user ->
                if (user != currentUser && user != null) {
                    currentUser = user
                }
            }
        }

        lifecycleScope.launch {
            viewModel.searchResults.collectLatest { results ->
                if (results.isNotEmpty()) {
                    searchResultsAdapter.submitList(results)
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    if (viewModel.hasSearched) {
                        toastFragment("No results found")
                    }
                }
            }
        }
        areasViewModel.featuresLiveData.observe(viewLifecycleOwner) { features ->
            val newCountries =
                features
                    .features()
                    ?.filter {
                        it.getStringProperty("placeId").isNotBlank()
                    }?.map { it.getStringProperty("placeId").toLong() }
                    .orEmpty()

            Log.d(TAG, "observeViewModel: $newCountries")
            searchResultsAdapter.updateCountries(newCountries)
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.searchIcon.visibility = if (isLoading) View.GONE else View.VISIBLE
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun onSearchResultClick(result: LocationSearchResult) {
        listener?.onLocationSelected(result) {
            binding.searchEditText.setText("")
        }
        isResultChosen = true
        binding.apply {
            searchEditText.setText(result.name)
            searchResultsAdapter.submitList(emptyList())
            searchResultsRecyclerView.visibility = View.GONE
            searchEditText.clearFocus()
            KeyboardUtils.hideKeyboard(searchEditText, requireContext())
        }
    }

    private fun onSearchResultRemove(result: LocationSearchResult) {
        lifecycleScope.launch {
            currentUser?.let { user ->
                try {
                    UserAreaManager(
                        UserRepository(requireContext()),
                        EventRepository(requireContext()),
                        AreasOfInterestRepository(requireContext()),
                    ).removeAreaOfInterest(
                        user.id,
                        AreaOfInterest(
                            result.osmId.toString(),
                            result.osmType.first().uppercase(),
                            result.name,
                            result.name,
                        ),
                    )

                    listener?.onAreaOfInterestChanged()
                    binding.apply {
                        searchResultsAdapter.submitList(emptyList())
                        searchEditText.setText("")
                        searchResultsRecyclerView.visibility = View.GONE
                        searchEditText.clearFocus()
                        KeyboardUtils.hideKeyboard(searchEditText, requireContext())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onRemove: Error removing area of interest", e)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
        searchRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    interface MapFragmentListener {
        fun onLocationSelected(
            searchResult: LocationSearchResult,
            onFinish: () -> Unit,
        )

        fun onAreaOfInterestChanged()
    }

    companion object {
        val TAG = "LocationSearchFragment"
    }
}
