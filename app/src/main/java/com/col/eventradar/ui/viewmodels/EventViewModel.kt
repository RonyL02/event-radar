package com.col.eventradar.ui.viewmodels

import android.net.Uri
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
import com.col.eventradar.models.common.PopulatedComment
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventViewModel(
    private val eventRepository: EventRepository,
    private val commentsRepository: CommentsRepository,
) : ViewModel() {
    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> get() = _events

    private val _comments = MutableLiveData<List<Comment>>(emptyList())
    val comments: LiveData<List<Comment>> get() = _comments

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _userComments = MutableLiveData<List<PopulatedComment>>(emptyList())
    val userComments: LiveData<List<PopulatedComment>> get() = _userComments

    fun syncAllComments() {
        viewModelScope.launch {
            commentsRepository.syncAllComments()
        }
    }

    fun fetchFilteredEvents(
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        alertLevels: List<AlertLevel>? = null,
        eventTypes: List<EventType>? = null,
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

    fun fetchCommentsForEvent(eventId: String) {
        viewModelScope.launch {
            _comments.postValue(
                _events.value
                    ?.find { it.id == eventId }
                    ?.comments
                    .orEmpty(),
            )

            try {
                commentsRepository.syncCommentsOfEvent(eventId)
                _comments.postValue(commentsRepository.getLocalComments(eventId))
            } catch (e: Exception) {
                _errorMessage.postValue("Failed fetch comments for eventId $eventId: ${e.localizedMessage}")
            }
        }
    }

    fun addComment(
        eventId: String,
        content: String,
        imageUri: Uri? = null,
    ) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                val newComment = commentsRepository.addCommentToEvent(eventId, content, imageUri)

                if (newComment != null) {
                    fetchCommentsForEvent(eventId)

                    _events.value =
                        _events.value?.map { event ->
                            if (event.id == eventId) {
                                event.copy(comments = event.comments + newComment)
                            } else {
                                event
                            }
                        }
                } else {
                    _errorMessage.postValue("Failed to add comment")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to add comment: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getEventsSince(since: LocalDateTime): LiveData<Int> = events.map { it.count { event -> event.time.isAfter(since) } }

    fun getEventsPerTypeSince(since: LocalDateTime): LiveData<Map<EventType, Int>> =
        events.map { eventsList ->
            eventsList
                .filter { it.time.isAfter(since) }
                .groupingBy { it.type }
                .eachCount()
        }

    fun deleteLocalEventsLeftovers() {
        viewModelScope.launch {
            eventRepository.deleteLocalEventsLeftovers()
        }
    }
}
