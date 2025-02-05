package com.col.eventradar.ui.components

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.col.eventradar.databinding.FragmentGpsLocationSearchBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.network.OpenStreetMapService
import com.col.eventradar.network.dto.toDomain
import com.col.eventradar.ui.views.MapFragment
import kotlinx.coroutines.launch

interface SearchBarEventListener {
    fun onFocusChange(value: Boolean)
}

class GpsLocationSearchFragment : Fragment(), SearchBarEventListener {
    private var bindingInternal: FragmentGpsLocationSearchBinding? = null
    private val binding get() = bindingInternal!!

    interface GpsLocationListener {
        fun onLocationReceived(location: LocationSearchResult)
        fun onGetLocation(): Location?
        fun onGPSLocationClick()
    }

    private var locationListener: GpsLocationListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentGpsLocationSearchBinding.inflate(inflater, container, false)
        binding.searchGpsLocationLayout.setOnClickListener {
            val location = locationListener?.onGetLocation()
            locationListener?.onGPSLocationClick()
            Log.d(TAG, location.toString())
            if (location != null) {
                binding.root.visibility = View.GONE
                lifecycleScope.launch {
                    try {
                        val recievedLocationResult = OpenStreetMapService.api.reverseGeocode(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        locationListener?.onLocationReceived(recievedLocationResult.toDomain())
                    } catch (e: retrofit2.HttpException) {
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        Toast.makeText(
                            requireContext(),
                            "Error fetching location: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "General error: ${e.message}")
                    }
                }
            }
        }
        binding.root.visibility = View.GONE
        return binding.root
    }

    private fun findMapFragment(): MapFragment? {
        var currentFragment: Fragment? = this
        while (currentFragment != null) {
            currentFragment = currentFragment.parentFragment
            if (currentFragment is MapFragment) {
                return currentFragment
            }
        }
        return null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            locationListener = findMapFragment()
        } catch (e: ClassCastException) {
            Log.e(TAG, "MapFragment not found or doesn't implement GpsLocationListener")
        }
    }

    companion object {
        val TAG = "GpsLocationSearchFragment"
    }

    override fun onFocusChange(value: Boolean) {
        if (value) {
            binding.root.visibility = View.VISIBLE
        } else {
            binding.root.visibility = View.GONE
        }
    }
}