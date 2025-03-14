package com.col.eventradar.data.remote

import android.content.Context
import com.col.eventradar.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.reflect.KProperty1

class UserRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val USERS_COLLECTION = "users"
    }

    suspend fun saveUser(user: User) {
        db.collection(USERS_COLLECTION).document(user.id).set(user.json).await()
    }

    suspend fun getUser(userId: String): User? {
        val document = db.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()

        val user = document.toObject(User::class.java)?.apply {
            id = document.id
        }

        return user
    }


    /**
     *
     *  updateUserField(
     *  "userId",
     *  User::locations,
     *  UserLocation("1234","sdfsfd","sdsdff"),
     *  "arrayRemove/arrayUnion")
     *
     *
     *
     *  updateUserField(
     *  "userId",
     *  User::username,
     *  "Jeff")
     */
    suspend fun <T : Any> updateUserField(
        userId: String,
        field: KProperty1<User, T>,
        value: T,
        operation: String = "set", // "set", "arrayUnion", or "arrayRemove"
    ) {
        val fieldName = field.name

        val updateValue: Any = when (operation) {
            "arrayUnion" -> FieldValue.arrayUnion(value)
            "arrayRemove" -> FieldValue.arrayRemove(value)
            else -> value
        }

        db.collection("users")
            .document(userId)
            .update(fieldName, updateValue).await()
    }
}
