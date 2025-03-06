package com.col.eventradar.data.remote

import android.content.Context
import com.col.eventradar.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val USERS_COLLECTION = "users"
    }

    suspend fun saveUser(user: User) {
        db.collection(USERS_COLLECTION).document(user.id).set(user.json).await()
    }

    suspend fun getUser(userId: String): User? {
        val user = db.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)

        user?.id = userId

        return user
    }
}