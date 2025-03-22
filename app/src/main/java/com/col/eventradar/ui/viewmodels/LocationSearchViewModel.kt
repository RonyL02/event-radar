package com.col.eventradar.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.api.locations.OpenStreetMapService
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.api.locations.dto.toModel
import com.col.eventradar.constants.LocationFilterConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LocationSearchViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<LocationSearchResult>>(emptyList())
    val searchResults: StateFlow<List<LocationSearchResult>> get() = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    var hasSearched = false

    fun searchLocation(query: String) {
        viewModelScope.launch {
            hasSearched = true
            _isLoading.value = true
            try {
                val results = OpenStreetMapService.api.searchLocation(query = query, limit = 5)
                _searchResults.value = results.map { it.toModel() }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("Error fetching location: $errorBody")
            } catch (e: Exception) {
                println("General error: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun searchCountryLocations(query: String) {
        viewModelScope.launch {
            hasSearched = true
            _isLoading.value = true
            try {
                val results =
                    OpenStreetMapService.api.searchLocation(
                        query = query,
                        limit = 50,
                        extraTags = 1,
                        type = LocationFilterConstants.TYPE_ADMINISTRATIVE,
                        classType = LocationFilterConstants.CLASS_BOUNDARY,
                    )

                Log.d(
                    "searchCountryLocations",
                    "searchCountryLocations: ${
                        results.map {
                            "(${it.type}, ${it.className}, ${it.name}, ${it.osmId}, ${it.osmType})"
                        }
                    }",
                )

                val countriesOnly =
                    results
                        .filter {
                            it.className == LocationFilterConstants.CLASS_BOUNDARY &&
                                it.type == LocationFilterConstants.TYPE_ADMINISTRATIVE &&
                                it.extraTags?.get(LocationFilterConstants.FIELD_LINKED_PLACE) ==
                                LocationFilterConstants.COUNTRY_LINKED_PLACE
                        }.map { it.toModel() }

                _searchResults.value = countriesOnly
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("Error fetching location: $errorBody")
            } catch (e: Exception) {
                println("General error: ${e.message}")
            }
            _isLoading.value = false
        }
    }
}
