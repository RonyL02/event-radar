package com.col.eventradar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import com.col.eventradar.R
import com.col.eventradar.ui.views.SettingsFragment

class MapFragment : Fragment() {
    private lateinit var mapView: MapView
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = rootView.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            map.setStyle(DEFAULT_MAP_STYLE_URL)
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(lat ?: DEFAULT_LAT, lon ?: DEFAULT_LON))
                .zoom(zoom ?: DEFAULT_ZOOM)
                .build()
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_ZOOM = "zoom"

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
