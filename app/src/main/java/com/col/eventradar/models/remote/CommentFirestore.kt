package com.col.eventradar.models.remote

import com.col.eventradar.models.common.Comment
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

data class CommentFirestore(
    val eventId: String = "", // 🔥 Each comment is linked to an event via eventId
    val content: String = "",
    val userId: String = "",
    val imageUrl: String? = null, // 🔥 Optional image attached to the comment
    @ServerTimestamp val timestamp: Date? = null, // Firestore auto-generated timestamp
) {
    @Exclude
    fun toDomain(): Comment =
        Comment(
            eventId = eventId,
            content = content,
            userId = userId,
            imageUrl = imageUrl,
            time =
                timestamp?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDateTime()
                    ?: LocalDateTime.now(),
        )
}
