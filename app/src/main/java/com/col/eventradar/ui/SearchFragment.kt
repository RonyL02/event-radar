package com.col.eventradar.ui.views

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
import com.col.eventradar.ui.adapters.SearchResultsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchFragment : Fragment() {

    private var bindingInternal: FragmentSearchBinding? = null
    private val binding get() = bindingInternal!!
    private var listener: MapFragmentListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isProgrammaticChange = false

    private val searchResultsAdapter = SearchResultsAdapter { result ->
        listener?.onLocationSelected(result)
        isProgrammaticChange = true
        binding.searchEditText.setText(result.name)
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
            override fun afterTextChanged(s: Editable?) {
                if (isProgrammaticChange) {
                    isProgrammaticChange = false
                    return
                }
                searchRunnable?.let {
                    handler.removeCallbacks(it)
                }

                searchRunnable = Runnable {
                    val query = s.toString()
                    if (query.isNotEmpty()) {
                        searchLocation(query)
                    }
                }
                searchRunnable?.let {
                    handler.postDelayed(it, 500)
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is MapFragmentListener) {
            listener = parentFragment as MapFragmentListener
        } else {
            throw RuntimeException("$parentFragment must implement MapFragmentListener")
        }
    }

    private fun searchLocation(query: String) {
        lifecycleScope.launch {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$BOUNDING_BOX_API?q=$encodedQuery&format=json&addressdetails=1&limit=$SEARCH_RESULT_LIMIT"
            try {
                // Show ProgressBar and hide Search Icon
                binding.progressBar.visibility = View.VISIBLE
                binding.searchIcon.visibility = View.GONE

                Log.d(TAG, "Sending request to $url")
                val jsonResponse = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.inputStream.bufferedReader().use { it.readText() }
                }

                Log.d(TAG, jsonResponse)

                val results = parseNominatimResponse(jsonResponse)
                if (results.isNotEmpty()) {
                    searchResultsAdapter.submitList(results)
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching location: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // Hide ProgressBar and show Search Icon
                binding.progressBar.visibility = View.GONE
                binding.searchIcon.visibility = View.VISIBLE
            }
        }
    }



    private fun parseNominatimResponse(response: String): List<SearchResult> {
        val resultList = mutableListOf<SearchResult>()
        val jsonArray = JSONArray(response)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("display_name")
            val lat = jsonObject.getDouble("lat")
            val lon = jsonObject.getDouble("lon")
            val address = jsonObject.optJSONObject("address")
            val state = address?.optString("state")
            val country = address?.optString("country")

            // Extract bounding box if available
            val boundingBox = jsonObject.optJSONArray("boundingbox")
            val southLat = boundingBox?.getDouble(0) ?: lat
            val northLat = boundingBox?.getDouble(1) ?: lat
            val westLon = boundingBox?.getDouble(2) ?: lon
            val eastLon = boundingBox?.getDouble(3) ?: lon

            resultList.add(SearchResult(state +", "+ country, lat, lon, southLat, northLat, westLon, eastLon))
        }
        return resultList
    }

    private fun handleSearchResult(searchResult: SearchResult) {
        listener?.onLocationSelected(searchResult)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
        searchRunnable?.let {
            handler.removeCallbacks(it) // Clear any pending tasks
        }

    }

    data class SearchResult(
        val name: String,
        val lat: Double,
        val lon: Double,
        val southLat: Double,
        val northLat: Double,
        val westLon: Double,
        val eastLon: Double
    )

    interface MapFragmentListener {
        fun onLocationSelected(searchResult: SearchResult)
    }

    companion object {
        val TAG = "SearchBar"
        val BOUNDING_BOX_API = "https://nominatim.openstreetmap.org/search"
        val SEARCH_RESULT_LIMIT = 6
    }
}
