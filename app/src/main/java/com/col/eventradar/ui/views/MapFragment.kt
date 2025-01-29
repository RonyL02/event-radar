package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import org.maplibre.android.MapLibre
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.models.LocationSearchResult
import com.col.eventradar.ui.LocationSearchFragment
import com.col.eventradar.ui.components.ToastFragment
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapLibreMap

class MapFragment : Fragment(), LocationSearchFragment.MapFragmentListener {
    private var bindingInternal: FragmentMapBinding? = null
    private val binding get() = bindingInternal!!

    private lateinit var map: MapLibreMap
    private lateinit var toastFragment: ToastFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
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

    private fun initMap() {
        binding.mapView.getMapAsync { map ->
            this.map = map
            MapUtils.initMap(map, requireContext(), toastFragment)
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
