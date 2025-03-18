package com.col.eventradar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.col.eventradar.models.local.CommentEntity

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("SELECT * FROM comments WHERE eventId = :eventId ORDER BY timestamp DESC")
    suspend fun getCommentsForEvent(eventId: String): List<CommentEntity>

    @Query("DELETE FROM comments WHERE eventId = :eventId")
    suspend fun deleteCommentsForEvent(eventId: String)
}
