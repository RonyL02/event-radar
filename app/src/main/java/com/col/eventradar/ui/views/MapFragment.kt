package com.col.eventradar.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.network.OpenStreetMapService
import com.col.eventradar.ui.LocationSearchFragment
import com.col.eventradar.ui.components.ToastFragment
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

class MapFragment : Fragment(), LocationSearchFragment.MapFragmentListener {
    private var bindingInternal: FragmentMapBinding? = null
    private val binding get() = bindingInternal!!

    private var lat: Double? = null
    private var lon: Double? = null
    private var zoom: Double? = null

    private lateinit var map: MapLibreMap
    private lateinit var toastFragment: ToastFragment

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

        toastFragment = ToastFragment()
        initMap()

        return binding.root
    }

    private fun initMap() {
        binding.mapView.getMapAsync { map ->
            this.map = map
            map.setStyle(DEFAULT_MAP_STYLE_URL) {
                setupInitialCameraPosition(map)
                addMapSourcesAndLayers(map)
            }
            map.addOnMapClickListener { point ->
                handleMapClick(point)
                true
            }
        }
    }

    private fun handleMapClick(point: LatLng) {
        val features = map.queryRenderedFeatures(map.projection.toScreenLocation(point), SEARCH_RESULT_AREA_LAYER_NAME) //TODO: Handle click only for event layers
        if (features.isNotEmpty()) {
            val feature = features.first()
            showFeatureContextMenu(feature, point)
        }
    }

    private fun showFeatureContextMenu(feature: Feature, point: LatLng) {
        toastFragment.showToast("Clicked on: ${feature.getStringProperty("localname")}")
    }

    private fun handleAddFeature(feature: Feature) {
        // Logic to handle the "Add" button click
    }

    private fun setupInitialCameraPosition(map: MapLibreMap) {
        map.cameraPosition = CameraPosition.Builder()
            .target(LatLng(lat ?: DEFAULT_LAT, lon ?: DEFAULT_LON))
            .zoom(zoom ?: DEFAULT_ZOOM)
            .build()
    }

    private fun addMapSourcesAndLayers(map: MapLibreMap) {
        val style = map.style ?: return

        style.addSource(GeoJsonSource(SEARCH_RESULT_AREA_SOURCE_NAME))
        val themeColor = getThemeColor()

        val fillLayer = FillLayer(SEARCH_RESULT_AREA_LAYER_NAME, SEARCH_RESULT_AREA_SOURCE_NAME).apply {
            withProperties(
                PropertyFactory.fillColor(themeColor),
                PropertyFactory.fillOpacity(0.3f)
            )
        }
        style.addLayer(fillLayer)
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
        val bounds = LatLngBounds.Builder()
            .include(LatLng(searchResult.southLat, searchResult.westLon))
            .include(LatLng(searchResult.northLat, searchResult.eastLon))
            .build()

        binding.mapView.getMapAsync { map ->
            lifecycleScope.launch {
                try {
                    val result = OpenStreetMapService.api.getLocationDetails(osmId = searchResult.osmId)
                    val feature = result.toMapLibreFeature()

                    map.style?.getSource(SEARCH_RESULT_AREA_SOURCE_NAME)?.let { source ->
                        if (source is GeoJsonSource) {
                            source.setGeoJson(FeatureCollection.fromFeature(feature))
                        }
                    }


                    //TODO: Check if user has the location already saved
                    binding.mapAddLocationButton.visibility = View.VISIBLE
                    binding.mapAddLocationButton.setOnClickListener {
                        toastFragment.showToast("Added ${feature.getStringProperty("localname")} to User")
                        Toast.makeText(requireContext(), "Added ${feature.getStringProperty("localname")} to User", Toast.LENGTH_SHORT).show()
                        binding.mapAddLocationButton.visibility = View.GONE
                    }

                } catch (e: retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                    toastFragment.showToast("Error fetching location: ${e.message}")
                        requireContext(),
                        "Error fetching location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "General error: ${e.message}")
                }
            }
            map.easeCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 150),
                500
            )
        }
    }

    private fun getThemeColor(): Int {
        val typedValue = android.util.TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
        return typedValue.data
    }

    companion object {
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_ZOOM = "zoom"

        val TAG = "Map"

        val SEARCH_RESULT_AREA_SOURCE_NAME = "seatch-result-area-source"
        val SEARCH_RESULT_AREA_LAYER_NAME = "location-fill-layer"

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
