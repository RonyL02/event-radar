package com.col.eventradar.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.User
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: String,
    val content: String,
    val userId: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

fun CommentEntity.toDomain(user: User?): Comment =
    Comment(
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        time = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC),
        user = user,
    )

fun Comment.toEntity(eventId: String): CommentEntity =
    CommentEntity(
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        timestamp = time.toEpochSecond(ZoneOffset.UTC) * 1000,
    )
