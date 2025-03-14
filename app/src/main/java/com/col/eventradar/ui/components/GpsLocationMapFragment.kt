package com.col.eventradar.ui.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.col.eventradar.databinding.FragmentGpsLocationMapBinding
import com.col.eventradar.utils.ThemeUtils
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class GpsLocationMapFragment : Fragment() {
    private var bindingInternal: FragmentGpsLocationMapBinding? = null
    private val binding get() = bindingInternal!!
    var locationComponent: LocationComponent? = null
    private var map: MapLibreMap? = null
    private var toastFragment: ToastFragment = ToastFragment()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Permission granted")
                enableLocationComponent(map?.style!!)
            } else {
                toastFragment("You need to accept location permissions.")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentGpsLocationMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun attachToMap(
        map: MapLibreMap,
        style: Style,
    ) {
        this.map = map
        enableLocationComponent(style)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            locationComponent = map?.locationComponent
            val accentColor = ThemeUtils.getThemeColor(requireContext())

            val locationComponentOptions =
                LocationComponentOptions
                    .builder(requireContext())
                    .accuracyColor(accentColor)
                    .pulseColor(accentColor)
                    .foregroundTintColor(accentColor)
                    .bearingTintColor(accentColor)
                    .maxZoomIconScale(1.0f)
                    .minZoomIconScale(1.0f)
                    .build()

            locationComponent?.activateLocationComponent(
                LocationComponentActivationOptions
                    .builder(requireContext(), style)
                    .locationComponentOptions(locationComponentOptions) // Apply the options
                    .build(),
            )

            locationComponent?.isLocationComponentEnabled = true
            locationComponent?.renderMode = RenderMode.COMPASS
        } else {
            checkPermissions(style)
        }
        Log.d(TAG, "Location component activated.")
    }

    private fun checkPermissions(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            enableLocationComponent(style)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun getCurrentLocation(): Location? {
        val location = locationComponent?.lastKnownLocation
        if (location != null) {
            return location
        } else if (!isLocationEnabled(requireContext())) {
            AlertDialog
                .Builder(context)
                .setTitle("Enable Location")
                .setMessage("Your location is turned off. Please enable it to use this feature.")
                .setPositiveButton("Enable") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity?.startActivity(intent)
                }.setNegativeButton("Cancel", null)
                .show()
        } else if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            toastFragment(
                "You need to accept location permissions to to use Location based services.",
            )
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            toastFragment("Location not available")
        }
        return null
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val locationMode =
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        return locationMode != Settings.Secure.LOCATION_MODE_OFF && (isGpsEnabled || isNetworkEnabled)
    }

    companion object {
        const val TAG = "GpsLocationMapFragment"
    }
}
