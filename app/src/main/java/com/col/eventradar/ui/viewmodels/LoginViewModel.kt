package com.col.eventradar.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.auth.GoogleAuthClient
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.User
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {
    private val authClient = GoogleAuthClient(context)
    private val userRepository = UserRepository(context)
    private val _isSignedIn = MutableLiveData<Boolean?>(null)
    val isSignedIn: LiveData<Boolean?> = _isSignedIn

    fun signIn() {
        viewModelScope.launch {
            val (isNew, id) = authClient.signIn() ?: (false to null)
            _isSignedIn.value = id != null


            if (!isNew) {
                println("user does not exist registering")
                registerUser()
            } else {
                println("not new")
            }
        }
    }

    private fun registerUser() {
        authClient.getCurrentUser()?.let { currentUser ->
            viewModelScope.launch {
                val newUser = User(
                    authClient.userId!!,
                    currentUser.displayName!!,
                    currentUser.photoUrl.toString()
                )
                userRepository.saveUser(newUser)
            }
        }
    }
}