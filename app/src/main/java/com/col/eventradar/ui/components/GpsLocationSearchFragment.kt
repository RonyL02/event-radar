package com.col.eventradar.ui.components

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.col.eventradar.R
import com.col.eventradar.databinding.FragmentGpsLocationSearchBinding
import com.col.eventradar.databinding.FragmentMapBinding
import com.col.eventradar.databinding.FragmentSearchBinding

class GpsLocationSearchFragment : Fragment() {
    private var bindingInternal: FragmentGpsLocationSearchBinding? = null
    private val binding get() = bindingInternal!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentGpsLocationSearchBinding.inflate(inflater, container, false)
        binding.searchGpsLocationLayout.setOnClickListener {
            Log.d(TAG,"Location GPS Clicked!")
        }
        return binding.root
    }

    companion object {
        val TAG = "GpsLocationSearchFragment"
    }
}