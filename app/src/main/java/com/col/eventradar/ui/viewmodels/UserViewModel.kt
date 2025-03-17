package com.col.eventradar.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.common.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser
    val userRepository: UserRepository = UserRepository.getInstance()

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> get() = _authState

    init {
        checkUserStatus()
    }

    /**
     * 🔥 **Check if a user is logged in & fetch user data**
     */
    fun checkUserStatus() {
        val firebaseUser: FirebaseUser? = userRepository.getCurrentUser()

        if (firebaseUser != null) {
            _authState.postValue(true)
            fetchUser(firebaseUser.uid)
        } else {
            _authState.postValue(false)
            _loggedInUser.postValue(null)
        }
    }

    /**
     * 🔥 **Fetch the logged-in user from Firestore**
     */
    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                _loggedInUser.postValue(user)
                Log.d(TAG, "User fetched: $user")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user data", e)
            }
        }
    }

    /**
     * 🔥 **Update the current user's profile in Firestore**
     */
    fun updateUserProfile(updatedUser: User) {
        viewModelScope.launch {
            try {
                userRepository.saveUser(updatedUser)
                _loggedInUser.postValue(updatedUser)
                Log.d(TAG, "User profile updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user profile", e)
            }
        }
    }

    /**
     * 🔥 **Log out the user**
     */
    fun logout() {
        userRepository.logoutUser()
        _loggedInUser.postValue(null)
        _authState.postValue(false)
        Log.d(TAG, "User logged out")
    }

    companion object {
        private const val TAG = "UserViewModel"
    }
}
