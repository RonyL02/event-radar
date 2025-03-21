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
    private val userRepository = UserRepository(context)
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
                    id = commentRef.id,
                    eventId = eventId,
                    content = content,
                    imageUrl = uploadedImageUrl,
                    userId = user?.uid ?: "Unknown",
                )

            commentRef.set(newComment.toFirestore(eventId)).await()

            commentDao.insertComment(newComment.toEntity())

            Log.d(TAG, "Comment added successfully with image: $uploadedImageUrl")
            return newComment
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            return null
        }
    }

    suspend fun updateComment(
        commentId: String,
        updatedContent: String?,
        newImageUri: Uri? = null,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val docRef = commentsCollection.document(commentId)
                val existingCommentSnapshot = docRef.get().await()
                val existingComment = existingCommentSnapshot.toObject(CommentFirestore::class.java)

                if (existingComment == null) {
                    Log.e(TAG, "Comment not found in Firestore: $commentId")
                    return@withContext
                }

                var updatedImageUrl: String? = existingComment.imageUrl

                newImageUri?.let {
                    updatedImageUrl = cloudinaryService.uploadImageToCloudinary(it)
                }

                val updatedComment =
                    existingComment.copy(
                        content = updatedContent ?: existingComment.content,
                        imageUrl = updatedImageUrl,
                    )

                docRef.set(updatedComment).await()

                commentDao.insertComment(updatedComment.toDomain().toEntity())

                Log.d(TAG, "Comment updated successfully: $commentId")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating comment", e)
            }
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

    suspend fun syncCommentsOfEvent(eventId: String) {
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

    suspend fun syncAllComments() =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Syncing all comments on app load...")

                val snapshot = commentsCollection.get().await()
                val remoteComments =
                    snapshot.toObjects(CommentFirestore::class.java).map { it.toDomain() }

                val localComments = commentDao.getAllComments()

                val commentsToUpdate = remoteComments.map { it.toEntity() }

                val remoteCommentIds = remoteComments.map { it.id }.toSet()
                val localCommentIds = localComments.map { it.id }.toSet()
                val commentsToDelete = localCommentIds - remoteCommentIds

                commentDao.insertComments(commentsToUpdate)
                commentsToDelete.forEach { commentDao.deleteCommentById(it) }

                Log.d(
                    TAG,
                    "Comments sync complete. Updated: ${commentsToUpdate.size}, Deleted: ${commentsToDelete.size}",
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing comments on app load", e)
            }
        }

    /**
     * Fetch Comments from Local Database (Room)
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
                val userComments = commentDao.getCommentsForUser(user.uid)

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

    suspend fun deleteComment(commentId: String) {
        withContext(Dispatchers.IO) {
            try {
                commentsCollection.document(commentId).delete().await()
                commentDao.deleteCommentById(commentId)
                Log.d(TAG, "Comment deleted: $commentId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting comment: $commentId", e)
            }
        }
    }

    companion object {
        const val COMMENTS_COLLECTION = "comments"
        const val TAG = "CommentsRepository"
    }
}
