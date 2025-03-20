package com.col.eventradar.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.col.eventradar.auth.GoogleAuthClient
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.common.User
import kotlinx.coroutines.launch

class LoginViewModel(
    context: Context,
) : ViewModel() {
    private val authClient = GoogleAuthClient(context)
    private val userRepository = UserRepository.getInstance()
    private val _isSignedIn = MutableLiveData<Boolean?>(null)
    val isSignedIn: LiveData<Boolean?> = _isSignedIn

    fun signIn() {
        viewModelScope.launch {
            authClient.signIn { (isNew, userId) ->
                _isSignedIn.value = userId != null
                if (isNew) {
                    println("user does not exist registering")
                    registerUser(userId!!)
                } else {
                    println("not new")
                }
            }
        }
    }

    private fun registerUser(newUserId: String) {
        authClient.getCurrentUser()?.let { currentUser ->
            viewModelScope.launch {
                val newUser =
                    User(
                        newUserId,
                        currentUser.displayName!!,
                        currentUser.photoUrl.toString(),
                    )
                userRepository.saveUser(newUser)
            }
        }
    }
}

class LoginViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
