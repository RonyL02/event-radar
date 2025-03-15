package com.col.eventradar.models.remote

import com.col.eventradar.models.common.Comment
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

data class CommentFirestore(
    val content: String = "",
    val username: String = "",
    @ServerTimestamp val timestamp: Date? = null, // Firestore auto-generated timestamp
)

fun CommentFirestore.toDomain(): Comment =
    Comment(
        content = content,
        username = username,
        time = timestamp?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDateTime() ?: LocalDateTime.now(),
    )

fun Comment.toFirestore(): CommentFirestore =
    CommentFirestore(
        content = content,
        username = username,
        timestamp = Date(time.toEpochSecond(ZoneOffset.UTC) * 1000),
    )
