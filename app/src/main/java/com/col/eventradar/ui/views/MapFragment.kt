package com.col.eventradar.ui.views

import MapUtils
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import org.maplibre.android.MapLibre
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.ui.LocationSearchFragment
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.utils.ThemeUtils
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class MapFragment : Fragment(), LocationSearchFragment.MapFragmentListener {
    private var bindingInternal: FragmentMapBinding? = null
    private val binding get() = bindingInternal!!

    private var isLocationPermitted = false
    private lateinit var map: MapLibreMap
    private lateinit var toastFragment: ToastFragment
    private var lastLocation: Location? = null
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
    }

    private fun getCurrentLocation() {
        val location = locationComponent?.lastKnownLocation
        if (location != null) {
            lastLocation = location
            val lat = location.latitude
            val lon = location.longitude
            Toast.makeText(context, "Lat: $lat, Lon: $lon", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkPermissions() {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            binding.mapView.getMapAsync {map ->
                Toast.makeText(
                    context,
                    "You have location permissions.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                    Toast.makeText(
                        context,
                        "You need to accept location permissions.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionResult(granted: Boolean) {
                    isLocationPermitted = granted
                    if (!granted) {
                        Toast.makeText(
                            context,
                            "You need to accept location permissions to to use Location based services.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            permissionsManager!!.requestLocationPermissions(activity)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentMapBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)

        toastFragment = ToastFragment()
        childFragmentManager.beginTransaction().add(toastFragment, "ToastFragment").commit()

        initMap()

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            locationComponent = map.locationComponent
            val accentColor = ThemeUtils.getThemeColor(requireContext())

            val locationComponentOptions = LocationComponentOptions.builder(requireContext())
                .accuracyColor(accentColor)
                .pulseColor(accentColor)
                .foregroundTintColor(accentColor)
                .bearingTintColor(accentColor)
                .build()

            locationComponent?.activateLocationComponent(
                LocationComponentActivationOptions.builder(requireContext(), style)
                    .locationComponentOptions(locationComponentOptions) // Apply the options
                    .build()
            )

            locationComponent?.isLocationComponentEnabled = true
            locationComponent?.renderMode = RenderMode.COMPASS
        } else {
            checkPermissions()
        }
        Log.d(TAG, "Location component activated.")
    }

    private fun initMap() {
        binding.mapView.getMapAsync { map ->
            this.map = map
//            MapUtils.initMap(map, requireContext(), toastFragment)
            map.setStyle(MapUtils.DEFAULT_MAP_STYLE_URL) { style ->
                MapUtils.setupInitialCameraPosition(map, MapUtils.DEFAULT_LAT, MapUtils.DEFAULT_LON, MapUtils.DEFAULT_ZOOM)
                MapUtils.addMapSourcesAndLayers(map, requireContext())
                enableLocationComponent(style)

                locationComponent?.lastKnownLocation?.let {
                    val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(it.latitude, it.longitude))
                        .zoom(10.0)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }

            map.addOnMapClickListener { point ->
                MapUtils.handleMapClick(map, point, toastFragment)
                true
            }
        }
    }

    override fun onLocationSelected(searchResult: LocationSearchResult) {
        binding.mapView.getMapAsync { map ->
            lifecycleScope.launch {
                MapUtils.handleLocationSelection(map, searchResult, toastFragment, binding)
            }
        }
    }

    override fun onStart() {
        super.onStart(); binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop(); binding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView(); binding.mapView.onDestroy(); bindingInternal = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState); binding.mapView.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG = "Map"
    }
}
