package com.col.eventradar.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.data.local.EventRepository
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventViewModel(
    private val repository: EventRepository,
) : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

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
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                val eventsList =
                    repository.fetchAndStoreEvents(
                        fromDate,
                        toDate,
                        alertLevels,
                        eventTypes,
                        country,
                    )

                if (eventsList.isEmpty()) {
                    _errorMessage.postValue("No events found.")
                    _events.postValue(emptyList())
                } else {
                    _events.postValue(eventsList)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching events: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchComments(eventId: String) {
        viewModelScope.launch {
            try {
                repository.syncCommentsFromFirestore(eventId) // 🔥 Try Firestore first
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync comments from Firestore", e)
            }
            _comments.postValue(repository.getLocalComments(eventId)) // 💾 Always load from Room
        }
    }

    fun addComment(
        eventId: String,
        comment: Comment,
    ) {
        viewModelScope.launch {
            repository.addCommentToEvent(eventId, comment) // 📤 Upload & Save
            fetchComments(eventId) // 🔄 Refresh UI
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

    companion object {
        private const val TAG = "EventViewModel"
    }
}
