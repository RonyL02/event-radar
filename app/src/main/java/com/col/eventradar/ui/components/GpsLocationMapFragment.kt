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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.col.eventradar.R
import com.col.eventradar.ui.views.MapFragment.Companion.TAG
import com.col.eventradar.utils.ThemeUtils
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style


class GpsLocationMapFragment : Fragment() {
    var locationComponent: LocationComponent? = null
    private var permissionsManager: PermissionsManager? = null
    private var map: MapLibreMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gps_location_map, container, false)
    }

    fun attachToMap(map: MapLibreMap, style: Style) {
        this.map = map
        enableLocationComponent(style)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            locationComponent = map?.locationComponent
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
            checkPermissions(style)
        }
        Log.d(TAG, "Location component activated.")
    }

    private fun checkPermissions(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            enableLocationComponent(style)
        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {}

                override fun onPermissionResult(granted: Boolean) {
                    if (!granted) {
                        Toast.makeText(
                            context,
                            "You need to accept location permissions.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        enableLocationComponent(style);
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

    fun getCurrentLocation(): Location? {
        val location = locationComponent?.lastKnownLocation
        if (location != null) {
            return location
        } else if (!isLocationEnabled(requireContext())) {
            AlertDialog.Builder(context)
                .setTitle("Enable Location")
                .setMessage("Your location is turned off. Please enable it to use this feature.")
                .setPositiveButton("Enable") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity?.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            Toast.makeText(
                context,
                "You need to accept location permissions to to use Location based services.",
                Toast.LENGTH_SHORT
            ).show()
            permissionsManager!!.requestLocationPermissions(activity)
        }
        else {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        return locationMode != Settings.Secure.LOCATION_MODE_OFF && (isGpsEnabled || isNetworkEnabled)
    }
}