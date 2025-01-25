package com.col.eventradar.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.ui.LocationSearchFragment
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds

class MapFragment : Fragment(), LocationSearchFragment.MapFragmentListener {
    private var bindingInternal: FragmentMapBinding? = null
    private val binding get() = bindingInternal!!

    private var lat: Double? = null
    private var lon: Double? = null
    private var zoom: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())

        arguments?.let {
            lat = it.getDouble(ARG_LAT, DEFAULT_LAT)
            lon = it.getDouble(ARG_LON, DEFAULT_LON)
            zoom = it.getDouble(ARG_ZOOM, DEFAULT_ZOOM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentMapBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync { map ->
            map.setStyle(DEFAULT_MAP_STYLE_URL)
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(lat ?: DEFAULT_LAT, lon ?: DEFAULT_LON))
                .zoom(zoom ?: DEFAULT_ZOOM)
                .build()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        bindingInternal = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLocationSelected(searchResult: LocationSearchResult) {
        Log.d(TAG, "onLocationSelected called")
        val bounds = LatLngBounds.Builder()
            .include(LatLng(searchResult.southLat, searchResult.westLon))
            .include(LatLng(searchResult.northLat, searchResult.eastLon))
            .build()

        binding.mapView.getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))  // 50px padding
        }
    }

    companion object {
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_ZOOM = "zoom"

        val TAG = "Map"

        private const val DEFAULT_LAT = 32.0
        private const val DEFAULT_LON = 35.0
        private const val DEFAULT_ZOOM = 5.0

        private const val DEFAULT_MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json";

        @JvmStatic
        fun newInstance(lat: Double, lon: Double, zoom: Double) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LAT, lat)
                    putDouble(ARG_LON, lon)
                    putDouble(ARG_ZOOM, zoom)
                }
            }
    }
}
