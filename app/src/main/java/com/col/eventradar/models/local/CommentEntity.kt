package com.col.eventradar.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.User
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val userId: String,
    val content: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

fun CommentEntity.toDomain(user: User?): Comment =
    Comment(
        id = id,
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        time = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC),
        user = user,
    )

fun CommentEntity.toDomain(): Comment =
    Comment(
        id = id,
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        time = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC),
    )

fun Comment.toEntity(eventId: String): CommentEntity =
    CommentEntity(
        id = id,
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        timestamp = time.toEpochSecond(ZoneOffset.UTC) * 1000,
    )

fun Comment.toEntity(): CommentEntity =
    CommentEntity(
        id = id,
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        timestamp = time.toEpochSecond(ZoneOffset.UTC) * 1000,
    )
