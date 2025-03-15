package com.col.eventradar.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.col.eventradar.models.common.Comment
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-increment ID
    val eventId: String, // Foreign Key linking to Event
    val content: String,
    val username: String,
    val timestamp: Long = System.currentTimeMillis(), // Store as epoch millis
)

fun CommentEntity.toDomain(): Comment =
    Comment(
        content = content,
        username = username,
        time = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC),
    )

fun Comment.toEntity(eventId: String): CommentEntity =
    CommentEntity(
        eventId = eventId,
        content = content,
        username = username,
        timestamp = time.toEpochSecond(ZoneOffset.UTC) * 1000,
    )
