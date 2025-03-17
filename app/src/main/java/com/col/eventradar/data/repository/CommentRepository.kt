package com.col.eventradar.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.col.eventradar.api.cloudinary.CloudinaryService
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.toFirestore
import com.col.eventradar.models.local.toDomain
import com.col.eventradar.models.local.toEntity
import com.col.eventradar.models.remote.CommentFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentsRepository(
    private val context: Context,
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository.getInstance()
    private val commentDao = AppLocalDatabase.getDatabase(context).commentDao()
    private val commentsCollection = firestore.collection(COMMENTS_COLLECTION)
    private val cloudinaryService = CloudinaryService(context)

    suspend fun getCommentsFromFirestore(eventId: String): List<Comment> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot =
                    commentsCollection
                        .whereEqualTo("eventId", eventId)
                        .get()
                        .await()

                val commentsFirestore = snapshot.toObjects(CommentFirestore::class.java)
                val comments = commentsFirestore.map { it.toDomain() }

                // Fetch user details for each comment
                val fullComments =
                    comments.map { comment ->
                        val user = userRepository.getUserById(comment.userId) // ✅ Fetch full user
                        comment.copy(user = user) // ✅ Attach full user details
                    }

                Log.d(TAG, "getCommentsFromFirestore: $fullComments")

                Log.d(
                    TAG,
                    "Fetched ${fullComments.size} comments with user details for event: $eventId",
                )
                return@withContext fullComments
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comments for event: $eventId", e)
                return@withContext emptyList()
            }
        }

    /**
     * ✅ **Adds a comment with an optional image**
     */
    suspend fun addCommentToEvent(
        eventId: String,
        content: String,
        imageUri: Uri? = null,
    ): Comment? {
        val commentRef = commentsCollection.document()

        try {
            var uploadedImageUrl: String? = null

            Log.d(TAG, "addCommentToEvent: $imageUri")

            // If an image is selected, upload to Cloudinary first
            imageUri?.let {
                uploadedImageUrl = cloudinaryService.uploadImageToCloudinary(it)
            }

            // ✅ Fetch the current user details
            val user = userRepository.getCurrentUser()

            // ✅ Create the Comment object inside the function
            val newComment =
                Comment(
                    eventId = eventId,
                    content = content,
                    imageUrl = uploadedImageUrl,
                    userId = user?.uid ?: "Unknown",
                )

            commentRef.set(newComment.toFirestore(eventId)).await()
            commentDao.insertComment(newComment.toEntity(eventId))

            Log.d(TAG, "Comment added successfully with image: $uploadedImageUrl")
            return newComment
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            return null
        }
    }

    suspend fun syncCommentsFromFirestore(eventId: String) {
        val comments = getCommentsFromFirestore(eventId)

        Log.d(TAG, "syncCommentsFromFirestore: $comments")

        if (comments.isNotEmpty()) {
            commentDao.deleteCommentsForEvent(eventId) // ✅ Clear old local comments

            val localComments =
                comments.map { comment ->
                    val user =
                        comment.user
                            ?: userRepository.getUserById(comment.userId) // Fetch user if missing
                    comment.copy(user = user).toEntity(eventId)
                }

            commentDao.insertComments(localComments) // ✅ Save new comments with full user
        }
    }

    /**
     * 💾 Fetch Comments from Local Database (Room)
     */
    suspend fun getLocalComments(eventId: String): List<Comment> =
        withContext(Dispatchers.IO) {
            val comments = commentDao.getCommentsForEvent(eventId)

            return@withContext comments.map { commentEntity ->
                val user =
                    userRepository.getUserById(commentEntity.userId) // ✅ Fetch user from Firestore
                commentEntity.toDomain(user) // ✅ Attach user to Comment
            }
        }

    companion object {
        const val COMMENTS_COLLECTION = "comments"
        const val TAG = "CommentsRepository"
    }
}
