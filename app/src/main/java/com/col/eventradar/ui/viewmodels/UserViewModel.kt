package com.col.eventradar.ui.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.models.common.PopulatedComment
import com.col.eventradar.models.common.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class UserViewModel(
    private val commentsRepository: CommentsRepository,
) : ViewModel() {
    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser
    val userRepository: UserRepository = UserRepository.getInstance()

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> get() = _authState

    private val _userComments = MutableLiveData<List<PopulatedComment>>()
    val userComments: LiveData<List<PopulatedComment>> get() = _userComments

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
            fetchLoggedInUserComments()
        } else {
            _authState.postValue(false)
            _loggedInUser.postValue(null)
            _userComments.postValue(emptyList()) // Clear user comments if logged out
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
     * 🔥 **Fetch comments for the logged-in user**
     */
    fun fetchLoggedInUserComments() {
        viewModelScope.launch {
            try {
                val userCommentsList = commentsRepository.getCommentsForLoggedInUser()
                _userComments.postValue(userCommentsList)
                Log.d(TAG, "User comments fetched: ${userCommentsList.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user comments", e)
            }
        }
    }

    /**
     *  **Update a Comment**
     */
    fun updateComment(
        commentId: String,
        updatedContent: String?,
        newImageUri: Uri?,
    ) {
        viewModelScope.launch {
            try {
                commentsRepository.updateComment(
                    commentId = commentId,
                    updatedContent = updatedContent,
                    newImageUri = newImageUri,
                )
                fetchLoggedInUserComments()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating comment", e)
            }
        }
    }

    /**
     * **Delete a Comment**
     */
    fun deleteCommentById(commentId: String) {
        viewModelScope.launch {
            try {
                commentsRepository.deleteComment(commentId)
                fetchLoggedInUserComments()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting comment", e)
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
        _userComments.postValue(emptyList()) // Clear user comments on logout
        Log.d(TAG, "User logged out")
    }

    companion object {
        private const val TAG = "UserViewModel"
    }
}
