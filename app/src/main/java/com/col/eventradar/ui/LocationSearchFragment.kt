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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.R
import com.col.eventradar.databinding.FragmentSearchBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.api.OpenStreetMapService
import com.col.eventradar.api.dto.toDomain
import com.col.eventradar.ui.adapters.LocationSearchResultsAdapter
import com.col.eventradar.ui.components.GpsLocationSearchFragment
import com.col.eventradar.utils.KeyboardUtils
import kotlinx.coroutines.launch

class LocationSearchFragment : Fragment() {
    private var bindingInternal: FragmentSearchBinding? = null
    private val binding get() = bindingInternal!!
    private var listener: MapFragmentListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isProgrammaticChange = false
    private var gpsFragment: GpsLocationSearchFragment? = null

    private val searchResultsAdapter = LocationSearchResultsAdapter { result ->
        listener?.onLocationSelected(result)
        isProgrammaticChange = true
        binding.apply {
            searchEditText.setText(result.name)
            searchResultsRecyclerView.visibility = View.GONE
            searchEditText.clearFocus()
            KeyboardUtils.hideKeyboard(searchEditText, requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentSearchBinding.inflate(inflater, container, false)

        gpsFragment = GpsLocationSearchFragment()

        childFragmentManager.beginTransaction()
            .add(R.id.gpsContainer, gpsFragment!!, "GpsFragmentTag")
            .commit()

        binding.apply {
            searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            searchResultsRecyclerView.adapter = searchResultsAdapter
            searchEditText.setOnFocusChangeListener { _, hasFocus ->
                gpsFragment?.onFocusChange(hasFocus)
            }
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(searchValue: Editable?) {
                    if (isProgrammaticChange) {
                        isProgrammaticChange = false
                        return
                    }
                    searchRunnable?.let {
                        handler.removeCallbacks(it)
                    }

                    searchRunnable = Runnable {
                        val query = searchValue.toString()
                        if (query.isNotEmpty()) {
                            searchLocation(query)
                        }
                    }
                    searchRunnable?.let {
                        val debounceDelayMillis = 500L
                        handler.postDelayed(it, debounceDelayMillis)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? MapFragmentListener
            ?: throw RuntimeException("$parentFragment must implement MapFragmentListener")
    }

    private fun searchLocation(query: String) {
        lifecycleScope.launch {
            try {
                setLoading(true)
                val results = OpenStreetMapService.api.searchLocation(query = query, limit = 5)
                val locationResults = results.map { it.toDomain() }
                Log.d(TAG, results.toString())
                if (results.isNotEmpty()) {
                    searchResultsAdapter.submitList(locationResults)
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "Error body: $errorBody")
                Toast.makeText(
                    requireContext(),
                    "Error fetching location: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "General error: ${e.message}")
            }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            searchIcon.visibility = if (isLoading) View.GONE else View.VISIBLE
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        fun onLocationSelected(searchResult: LocationSearchResult)
    }

    companion object {
        val TAG = "LocationSearchFragment"
    }
}
