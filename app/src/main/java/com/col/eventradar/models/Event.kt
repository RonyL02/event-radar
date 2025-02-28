package com.col.eventradar.models

import java.time.LocalDateTime

/**
 * A placeholder item representing a piece of content.
 */
data class Event(
    val id: String,
    val title: String,
    val content: String,
    val location: Location?,
    val city: String,
    val date: String,
    val type: String,
    val details: String,
) {
    override fun toString(): String = content
}

enum class EventType {
    EarthQuake
}

data class EventDetails(
    val type: EventType,
    val name: String,
    val locationName: String,
    val time: LocalDateTime,
    val description: String,
    val commentsAmount: Int,
)