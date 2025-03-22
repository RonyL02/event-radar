package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.api.locations.OpenStreetMapService
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.api.locations.dto.toModel
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
                        limit = 10,
                        extraTags = 1,
                    )

                val countriesOnly =
                    results
                        .filter {
                            it.className == "boundary" &&
                                it.type == "administrative" &&
                                it.extraTags?.get("admin_level") == "2"
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
