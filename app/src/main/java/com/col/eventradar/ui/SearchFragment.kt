package com.col.eventradar.ui.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.col.eventradar.databinding.FragmentSearchBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentSearchBinding.inflate(inflater, container, false)

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                searchLocation(query)
            } else {
                Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun handleSearchResult(searchResult: SearchResult) {
        listener?.onLocationSelected(searchResult)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun searchLocation(query: String) {
        lifecycleScope.launch {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=1"
            try {
                Log.d(TAG,"Sending request to " + url)
                val jsonResponse = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.inputStream.bufferedReader().use { it.readText() }
                }

                Log.d(TAG, jsonResponse.toString())

                val result = parseNominatimResponse(jsonResponse)
                if (result != null) {
                    handleSearchResult(result);
                } else {
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseNominatimResponse(response: String): SearchResult? {
        val jsonArray = JSONArray(response)
        if (jsonArray.length() > 0) {
            val jsonObject = jsonArray.getJSONObject(0)
            val lat = jsonObject.getDouble("lat")
            val lon = jsonObject.getDouble("lon")

            val boundingBox = jsonObject.getJSONArray("boundingbox")
            val southLat = boundingBox.getDouble(0)
            val northLat = boundingBox.getDouble(1)
            val westLon = boundingBox.getDouble(2)
            val eastLon = boundingBox.getDouble(3)

            return SearchResult(lat, lon, southLat, northLat, westLon, eastLon)
        }
        return null
    }
    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    data class SearchResult(
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
    }
}
