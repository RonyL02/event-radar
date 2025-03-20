package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository

class EventViewModelFactory(
    private val eventRepository: EventRepository,
    private val commentRepository: CommentsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(eventRepository, commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
