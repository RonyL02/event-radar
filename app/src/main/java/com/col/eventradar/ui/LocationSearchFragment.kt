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
import com.col.eventradar.databinding.FragmentSearchBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.network.OpenStreetMapService
import com.col.eventradar.network.dto.toDomain
import com.col.eventradar.ui.adapters.LocationSearchResultsAdapter
import kotlinx.coroutines.launch
import org.json.JSONArray

class LocationSearchFragment : Fragment() {

    private var bindingInternal: FragmentSearchBinding? = null
    private val binding get() = bindingInternal!!
    private var listener: MapFragmentListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isProgrammaticChange = false

    private val searchResultsAdapter = LocationSearchResultsAdapter { result ->
        listener?.onLocationSelected(result)
        isProgrammaticChange = true
        binding.searchEditText.setText(result.locationName)
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchEditText.clearFocus()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecyclerView.adapter = searchResultsAdapter

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
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
