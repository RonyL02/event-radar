package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.models.Event
import com.col.eventradar.models.EventType
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventViewModel : ViewModel() {
    private val repository = GdacsService()

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchFilteredEvents(
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        alertLevels: List<AlertLevel>? = null,
        eventTypes: List<EventType>? = null,
        country: String? = null,
    ) {
        _isLoading.postValue(true) // Show loading

        viewModelScope.launch {
            try {
                val response =
                    repository.fetchEvents(fromDate, toDate, alertLevels, eventTypes, country)

                if (response == null) {
                    _errorMessage.postValue("No events found.")
                    _events.postValue(emptyList()) // Clear existing events
                } else {
                    _events.postValue(response.toDomain()) // Convert to Event model
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching events: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false) // Hide loading
            }
        }
    }

    fun getEventsSince(since: LocalDateTime): LiveData<Int> =
        events.map { eventsList ->
            eventsList.count { it.time.isAfter(since) }
        }

    fun getEventsPerTypeSince(since: LocalDateTime): LiveData<Map<EventType, Int>> =
        events.map { eventsList ->
            eventsList
                .filter { it.time.isAfter(since) }
                .groupingBy { it.type }
                .eachCount()
        }
}
