package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository

class UserViewModelFactory(
    private val commentsRepository: CommentsRepository,
    private val userRepository: UserRepository,
    private val areasRepository: AreasOfInterestRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(commentsRepository, userRepository, areasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
