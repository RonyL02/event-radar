package com.col.eventradar.models.common

import com.col.eventradar.models.remote.CommentFirestore
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

data class Comment(
    val id: String = "",
    val eventId: String = "",
    val content: String? = "",
    val time: LocalDateTime = LocalDateTime.now(),
    val imageUrl: String? = null,
    val userId: String = "",
    val user: User? = null,
) {
    fun hasImage() = !imageUrl.isNullOrBlank()
}

data class PopulatedComment(
    val comment: Comment,
    val event: Event,
)

fun Comment.toFirestore(eventId: String): CommentFirestore =
    CommentFirestore(
        eventId = eventId,
        content = content,
        userId = userId,
        imageUrl = imageUrl,
        timestamp = Date.from(time.toInstant(ZoneOffset.UTC)),
    )
