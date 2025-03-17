package com.col.eventradar.data.remote

import android.util.Log
import com.col.eventradar.models.common.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.reflect.KProperty1

class UserRepository private constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection(USERS_COLLECTION)

    companion object {
        const val USERS_COLLECTION = "users"
        const val TAG = "UserRepository"

        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository().also { instance = it }
            }
    }

    /**
     * 🔥 **Check if a user is logged in**
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * 🔥 **Get the currently logged-in user**
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun getUserById(userId: String): User? =
        withContext(Dispatchers.IO) {
            try {
                val snapshot =
                    usersCollection
                        .document(userId)
                        .get()
                        .await()

                return@withContext snapshot.toObject(User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user for ID: $userId", e)
                return@withContext null
            }
        }

    /**
     * 🔥 **Save or update user data in Firestore**
     */
    suspend fun saveUser(user: User) {
        try {
            usersCollection.document(user.id).set(user).await()
            Log.d(TAG, "User saved successfully: ${user.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore", e)
        }
    }

    /**
     * 🔥 **Update a single user field in Firestore**
     */
    suspend fun <T : Any> updateUserField(
        userId: String,
        field: KProperty1<User, T>,
        value: T,
        operation: UpdateOperations = UpdateOperations.Set,
    ) {
        val fieldName = field.name
        val updateValue: Any =
            when (operation) {
                UpdateOperations.ArrayUnion -> FieldValue.arrayUnion(value)
                UpdateOperations.ArrayRemove -> FieldValue.arrayRemove(value)
                else -> value
            }

        try {
            usersCollection.document(userId).update(fieldName, updateValue).await()
            Log.d(TAG, "Successfully updated $fieldName for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user field: $fieldName for $userId", e)
        }
    }

    /**
     * 🔥 **Delete a user from Firestore**
     */
    suspend fun deleteUser(userId: String) {
        try {
            usersCollection.document(userId).delete().await()
            Log.d(TAG, "User deleted successfully: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: $userId", e)
        }
    }

    /**
     * 🔥 **Log out the current user**
     */
    fun logoutUser() {
        auth.signOut()
        Log.d(TAG, "User logged out successfully")
    }

    enum class UpdateOperations {
        Set,
        ArrayUnion,
        ArrayRemove,
    }
}
