package com.col.eventradar.models.remote

import com.col.eventradar.models.common.Comment
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

data class CommentFirestore(
    @DocumentId val id: String = "",
    val eventId: String = "",
    val content: String? = "",
    val userId: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp val timestamp: Date? = null,
) {
    @Exclude
    fun toDomain(): Comment =
        Comment(
            id = id,
            eventId = eventId,
            content = content ?: "",
            userId = userId,
            imageUrl = imageUrl,
            time =
                timestamp?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDateTime()
                    ?: LocalDateTime.now(),
        )
}
