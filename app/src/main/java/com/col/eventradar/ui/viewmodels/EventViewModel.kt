package com.col.eventradar.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventViewModel(
    private val eventRepository: EventRepository,
    private val commentsRepository: CommentsRepository,
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
                    eventRepository.fetchAndStoreEvents(
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
            // ✅ Step 1: Fetch comments directly from the already loaded events list
            val eventComments =
                _events.value
                    ?.find { it.id == eventId }
                    ?.comments
                    .orEmpty()
            _comments.postValue(eventComments)

            Log.d(TAG, "fetchComments (from _events): $eventComments")

            try {
                // 🔥 Step 2: Fetch latest comments from Firestore and update Room DB
                commentsRepository.syncCommentsFromFirestore(eventId)

                // 💾 Step 3: Fetch the updated comments from Room again after sync
                val updatedComments = commentsRepository.getLocalComments(eventId)
                _comments.postValue(updatedComments)

                Log.d(TAG, "fetchComments (after sync): $updatedComments")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync comments from Firestore", e)
            }
        }
    }

    fun addComment(
        eventId: String,
        content: String,
        imageUri: Uri? = null,
    ) {
        viewModelScope.launch {
            // 📤 Upload & Save comment (Returns the created Comment)
            val newComment = commentsRepository.addCommentToEvent(eventId, content, imageUri)

            if (newComment != null) {
                // 🔄 Refresh comments for this event
                fetchComments(eventId)

                // 🔄 Update events list with the newly added comment
                _events.value =
                    _events.value?.map { event ->
                        if (event.id == eventId) {
                            event.copy(comments = event.comments + newComment) // ✅ Append new comment
                        } else {
                            event
                        }
                    }

                Log.d(
                    TAG,
                    "addComment: Successfully added comment to eventId=$eventId | Content=$content",
                )
            } else {
                Log.e(TAG, "addComment: Failed to add comment for eventId=$eventId")
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

    companion object {
        private const val TAG = "EventViewModel"
    }
}
