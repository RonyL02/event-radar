package com.col.eventradar.ui.viewmodels

import MapUtils
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.api.locations.OpenStreetMapService
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.models.common.PopulatedComment
import com.col.eventradar.models.common.User
import com.col.eventradar.utils.GeoJsonParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(
    private val commentsRepository: CommentsRepository,
    private val userRepository: UserRepository,
    private val areasRepository: AreasOfInterestRepository,
) : ViewModel() {
    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser

    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> get() = _authState

    private val _userComments = MutableLiveData<List<PopulatedComment>>()
    val userComments: LiveData<List<PopulatedComment>> get() = _userComments
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    private var startedObserving = false

    init {
        observeAuthState()
        observeAndSyncUserAreas()
    }

    private fun observeAuthState() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            viewModelScope.launch {
                val firebaseUser = auth.currentUser
                _user.value = firebaseUser?.let { userRepository.getUserById(it.uid) }
            }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                _user.value = userRepository.getUserById(uid)
            }
        }
    }

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
            _userComments.postValue(emptyList())
        }
    }

    /**
     * 🔥 **Fetch the logged-in user from Firestore**
     */
    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val newUser = userRepository.getUserById(userId)
                _loggedInUser.postValue(newUser)
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

    fun updateUserProfile(
        updatedUsername: String? = null,
        newProfileImageUri: Uri? = null,
    ) {
        viewModelScope.launch {
            try {
                val currentUser = _loggedInUser.value ?: return@launch
                Log.d(TAG, "updateUserProfile: $currentUser")
                userRepository.updateUser(
                    userId = currentUser.id,
                    updatedUsername = updatedUsername,
                    newProfileImageUri = newProfileImageUri,
                )

                val refreshedUser = userRepository.getUserById(currentUser.id)
                _loggedInUser.postValue(refreshedUser)

                Log.d(TAG, "User profile updated successfully: $refreshedUser")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user profile", e)
            }
        }
    }

    /**
     * **Log out the user**
     */
    suspend fun logout() {
        userRepository.logoutUser()

        _loggedInUser.postValue(null)
        _authState.postValue(false)
        _userComments.postValue(emptyList())

        Log.d(TAG, "User logged out")
    }

    fun observeAndSyncUserAreas() {
        if (startedObserving) return
        startedObserving = true

        viewModelScope.launch {
            var lastUser: User? = null

            user.collect { newUser ->
                if (newUser != null && (newUser != lastUser || newUser.areasOfInterest.toSet() != lastUser?.areasOfInterest?.toSet())) {
                    Log.d("UserViewModel", "User or areas changed: ${newUser.username}")
                    lastUser = newUser

                    val storedCountries = areasRepository.getStoredFeatures().map { it.placeId }

                    val missing =
                        newUser.areasOfInterest.filterNot { it.placeId in storedCountries }
                    Log.d("UserViewModel", "Missing countries: ${missing.map { it.name }}")

                    if (missing.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            val deferred =
                                missing.map { area ->
                                    async {
                                        area.placeId.toLongOrNull()?.let { placeIdLong ->
                                            try {
                                                val result =
                                                    OpenStreetMapService.api.getLocationDetails(
                                                        placeIdLong,
                                                    )
                                                val feature = MapUtils.toMapLibreFeature(result)
                                                val jsonData = GeoJsonParser.gson.toJson(feature)
                                                areasRepository.saveFeature(feature, jsonData)
                                                Log.d(
                                                    "UserViewModel",
                                                    "Saved feature for: ${area.name}",
                                                )
                                            } catch (e: Exception) {
                                                Log.e(
                                                    "UserViewModel",
                                                    "Error loading area ${area.name}",
                                                    e,
                                                )
                                            }
                                        }
                                    }
                                }
                            deferred.awaitAll()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "UserViewModel"
    }
}
