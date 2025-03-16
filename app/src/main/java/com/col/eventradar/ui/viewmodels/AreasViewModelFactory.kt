package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.col.eventradar.data.local.AreasOfInterestRepository

class AreasViewModelFactory(
    private val repository: AreasOfInterestRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AreasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AreasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
