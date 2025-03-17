package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            viewModelScope.launch {
                val firebaseUser = auth.currentUser
                _user.value = firebaseUser?.let { repository.getUser(it.uid) }
            }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                _user.value = repository.getUser(uid)
            }
        }
    }
}