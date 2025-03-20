package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.col.eventradar.data.repository.CommentsRepository

class UserViewModelFactory(
    private val commentsRepository: CommentsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(commentsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
