package com.col.eventradar.ui.views

import MapUtils
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.data.EventRepository
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.Event
import com.col.eventradar.ui.LocationSearchFragment
import com.col.eventradar.ui.components.GpsLocationMapFragment
import com.col.eventradar.ui.components.GpsLocationSearchFragment
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.col.eventradar.utils.addEventIconsToMap
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource

class MapFragment :
    Fragment(),
    LocationSearchFragment.MapFragmentListener,
    GpsLocationSearchFragment.GpsLocationListener {
    private var bindingInternal: FragmentMapBinding? = null
    private val binding get() = bindingInternal!!

    private lateinit var map: MapLibreMap
    private lateinit var toastFragment: ToastFragment
    private lateinit var locationFragment: GpsLocationMapFragment


    private val eventViewModel: EventViewModel by activityViewModels {
        val repository = EventRepository(requireContext())
        EventViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentMapBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)
        locationFragment = GpsLocationMapFragment()
        childFragmentManager
            .beginTransaction()
            .add(locationFragment, GpsLocationMapFragment.TAG)
            .commit()

        toastFragment = ToastFragment()
        childFragmentManager.beginTransaction().add(toastFragment, ToastFragment.TAG).commit()

        initMap()

        return binding.root
    }

    private fun initMap() {
        binding.mapView.getMapAsync { map ->
            this.map = map
            map.setStyle(MapUtils.DEFAULT_MAP_STYLE_URL) { style ->
                MapUtils.setupInitialCameraPosition(
                    map,
                    MapUtils.DEFAULT_LAT,
                    MapUtils.DEFAULT_LON,
                    MapUtils.DEFAULT_ZOOM,
                )
                lifecycleScope.launch {
                    MapUtils.addMapSourcesAndLayers(map, requireContext())
                    addEventIconsToMap(style, requireContext())
                }
                observeViewModel(style)
                locationFragment.attachToMap(map, style)
            }

            map.addOnMapClickListener { point ->
                MapUtils.handleMapClick(map, point, toastFragment)
                true
            }

            fetchEvents()
        }
    }

    override fun onLocationSelected(searchResult: LocationSearchResult) {
        binding.mapView.getMapAsync { map ->
            lifecycleScope.launch {
                MapUtils.handleLocationSelection(map, searchResult, toastFragment, binding)
            }
        }
    }

    private fun observeViewModel(style: Style) {
        eventViewModel.events.observe(viewLifecycleOwner) { events ->
            if (::map.isInitialized) {
                updateMapWithEvents(events, style)
            } else {
                binding.mapView.getMapAsync {
                    updateMapWithEvents(events, style)
                }
            }
        }
    }

    private fun fetchEvents() {
        eventViewModel.fetchFilteredEvents()
    }

    private fun updateMapWithEvents(events: List<Event>, style: Style) {
        val geoJson = MapUtils.convertEventsToGeoJson(events)
        style.getSourceAs<GeoJsonSource>(MapUtils.EVENT_SOURCE_NAME)?.setGeoJson(geoJson)
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

    override fun onLocationReceived(location: LocationSearchResult) {
        onLocationSelected(location)
    }

    override fun onGPSLocationClick() {
        locationFragment.locationComponent?.lastKnownLocation?.let {
            val cameraPosition =
                CameraPosition
                    .Builder()
                    .target(LatLng(it.latitude, it.longitude))
                    .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    override fun onGetLocation(): Location? = locationFragment.getCurrentLocation()
}
