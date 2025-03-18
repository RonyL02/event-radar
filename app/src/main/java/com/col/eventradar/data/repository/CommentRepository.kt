package com.col.eventradar.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.col.eventradar.api.cloudinary.CloudinaryService
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.PopulatedComment
import com.col.eventradar.models.common.toFirestore
import com.col.eventradar.models.local.toDomain
import com.col.eventradar.models.local.toEntity
import com.col.eventradar.models.remote.CommentFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommentsRepository(
    private val context: Context,
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository.getInstance()
    private val eventRepository = EventRepository(context)
    private val commentDao = AppLocalDatabase.getDatabase(context).commentDao()
    private val commentsCollection = firestore.collection(COMMENTS_COLLECTION)
    private val cloudinaryService = CloudinaryService(context)

    private suspend fun getCommentsFromFirestore(eventId: String): List<Comment> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot =
                    commentsCollection
                        .whereEqualTo("eventId", eventId)
                        .get()
                        .await()

                val commentsFirestore = snapshot.toObjects(CommentFirestore::class.java)
                val comments = commentsFirestore.map { it.toDomain() }

                val fullComments =
                    comments.map { comment ->
                        val user = userRepository.getUserById(comment.userId)
                        comment.copy(user = user)
                    }

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
     *  **Adds a comment with an optional image**
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

            imageUri?.let {
                uploadedImageUrl = cloudinaryService.uploadImageToCloudinary(it)
            }

            val user = userRepository.getCurrentUser()

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

    suspend fun getCommentsForEvents(eventIds: List<String>): Map<String, List<Comment>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                if (eventIds.isEmpty()) return@withContext emptyMap()

                val batchSize = 30
                val eventIdChunks = eventIds.chunked(batchSize)

                val deferredResults =
                    eventIdChunks.map { chunk ->
                        async {
                            commentsCollection
                                .whereIn("eventId", chunk)
                                .get()
                                .await()
                                .documents
                                .mapNotNull {
                                    it.toObject(CommentFirestore::class.java)?.toDomain()
                                }
                        }
                    }

                val commentsMap =
                    deferredResults
                        .awaitAll()
                        .flatten()
                        .groupBy { it.eventId }

                val comments =
                    commentsMap.values
                        .flatten()
                        .toList()
                        .map { it.toEntity() }

                commentDao.insertComments(comments)

                commentsMap
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comments for events", e)
                emptyMap()
            }
        }

    suspend fun syncCommentsFromFirestore(eventId: String) {
        val comments = getCommentsFromFirestore(eventId)

        if (comments.isNotEmpty()) {
            commentDao.deleteCommentsForEvent(eventId)

            val localComments =
                comments.map { comment ->
                    val user =
                        comment.user
                            ?: userRepository.getUserById(comment.userId)
                    comment.copy(user = user).toEntity(eventId)
                }

            commentDao.insertComments(localComments)
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
                    userRepository.getUserById(commentEntity.userId)
                commentEntity.toDomain(user)
            }
        }

    /**
     * **Fetch Comments for the Logged-in User**
     */
    suspend fun getCommentsForLoggedInUser(): List<PopulatedComment> =
        withContext(Dispatchers.IO) {
            val user = userRepository.getCurrentUser() ?: return@withContext emptyList()

            Log.d(TAG, "getCommentsForLoggedInUser: $user")
            try {
                val userComments = commentDao.getCommentsForUser(user.uid) // ✅ Fetch from DAO

                val populatedComments =
                    userComments.mapNotNull { commentEntity ->
                        val event = eventRepository.getLocalEventById(commentEntity.eventId)
                        event?.let { PopulatedComment(commentEntity.toDomain(), it) }
                    }

                Log.d(TAG, "Fetched ${populatedComments.size} user comments from local DB.")

                Log.d(TAG, "comments: $userComments")
                return@withContext populatedComments
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user comments from local DB", e)
                return@withContext emptyList()
            }
        }

    companion object {
        const val COMMENTS_COLLECTION = "comments"
        const val TAG = "CommentsRepository"
    }
}
