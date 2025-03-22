package com.col.eventradar.ui.views

import MapUtils
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.col.eventradar.api.locations.OpenStreetMapService
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.User
import com.col.eventradar.ui.LocationSearchFragment
import com.col.eventradar.ui.components.GpsLocationMapFragment
import com.col.eventradar.ui.components.GpsLocationSearchFragment
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.ui.viewmodels.AreasViewModel
import com.col.eventradar.ui.viewmodels.AreasViewModelFactory
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory
import com.col.eventradar.utils.GeoJsonParser
import com.col.eventradar.utils.addEventIconsToMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection

class MapFragment :
    Fragment(),
    LocationSearchFragment.MapFragmentListener,
    GpsLocationSearchFragment.GpsLocationListener {
    private var bindingInternal: FragmentMapBinding? = null
    private val binding get() = bindingInternal!!

    private lateinit var map: MapLibreMap
    private lateinit var toastFragment: ToastFragment
    private lateinit var locationFragment: GpsLocationMapFragment
    private var areasOfInterest: FeatureCollection? = null
    private var currentUser: User? = null

    private val eventViewModel: EventViewModel by activityViewModels {
        val eventRepository = EventRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        EventViewModelFactory(eventRepository, commentRepository)
    }

    private val areasViewModel: AreasViewModel by activityViewModels {
        val repository = AreasOfInterestRepository(requireContext())
        AreasViewModelFactory(repository)
    }

    private val userViewModel: UserViewModel by activityViewModels {
        val repository = UserRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        UserViewModelFactory(commentRepository, repository)
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
        }
    }

    override fun onAreaOfInterestChanged() {
        areasViewModel.refresh()
        eventViewModel.fetchFilteredEvents()
    }

    override fun onLocationSelected(
        searchResult: LocationSearchResult,
        onFinish: () -> Unit,
    ) {
        binding.mapView.getMapAsync { map ->
            lifecycleScope.launch {
                val areas =
                    currentUser
                        ?.areasOfInterest
                        ?.map { areaOfInterest -> areaOfInterest.placeId } ?: emptyList()

                MapUtils.handleLocationSelection(
                    map,
                    searchResult,
                    toastFragment,
                    binding,
                    onFinish = onFinish,
                    countries = areas,
                )
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

        areasViewModel.featuresLiveData.observe(viewLifecycleOwner) { features ->
            if (areasOfInterest != features) {
                areasOfInterest = features
                MapUtils.setSourceFeatures(
                    style,
                    MapUtils.AREAS_OF_INTEREST_SOURCE_NAME,
                    features,
                )
                fetchEvents()
            }
        }

        lifecycleScope.launch {
            userViewModel.user.collect { user ->
                if ((user != currentUser && user != null) || user?.areasOfInterest?.toSet() != currentUser?.areasOfInterest?.toSet()) {
                    currentUser = user

                    val areasRepo = AreasOfInterestRepository(requireContext())
                    val countries = areasRepo.getStoredFeatures().map { it.placeId }

                    val missingCountries =
                        user?.areasOfInterest?.filterNot { it.placeId in countries } ?: emptyList()

                    if (missingCountries.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            val deferredRequests =
                                missingCountries.map { area ->
                                    async {
                                        area.placeId.toLongOrNull()?.let { placeIdLong ->
                                            try {
                                                val result =
                                                    OpenStreetMapService.api.getLocationDetails(
                                                        placeIdLong,
                                                    )
                                                val feature = MapUtils.toMapLibreFeature(result)
                                                val jsonData = GeoJsonParser.gson.toJson(feature)
                                                areasRepo.saveFeature(feature, jsonData)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            deferredRequests.awaitAll()
                        }
                    }
                }
            }
        }
    }

    private fun fetchEvents() {
        eventViewModel.fetchFilteredEvents()
    }

    private fun updateMapWithEvents(
        events: List<Event>,
        style: Style,
    ) {
        Log.d("updateMapWithEvents", "updateMapWithEvents: ${events.size}")
        val geoJson = MapUtils.convertEventsToGeoJson(events)
        style.getSourceAs<GeoJsonSource>(MapUtils.EVENT_SOURCE_NAME)?.setGeoJson(geoJson)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        eventViewModel.refreshEvents()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        eventViewModel.refreshEvents()
        areasViewModel.refresh()
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

    override fun onLocationReceived(
        location: LocationSearchResult,
        onFinish: () -> Unit,
    ) {
        onLocationSelected(location, onFinish)
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

    private fun deleteLocalEventsLeftovers() {
        viewLifecycleOwner.lifecycleScope.launch {
            eventViewModel.deleteLocalEventsLeftovers()
        }
    }

    override fun onGetLocation(): Location? = locationFragment.getCurrentLocation()
}
