package com.col.eventradar.models.remote

import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import com.col.eventradar.models.common.Location
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class EventFirestore(
    val eventid: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val location: Location = Location(0.0, 0.0),
    val locationName: String = "",
    val time: Long = System.currentTimeMillis(), // Stored as epoch millis
    val comments: List<Comment> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis(),
)

fun EventFirestore.toDomain(): Event =
    Event(
        id = eventid,
        title = name,
        location = location,
        locationName = locationName,
        time = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC),
        type = EventType.fromString(type),
        description = description,
        comments = comments,
    )

fun Event.toFirestore(): EventFirestore =
    EventFirestore(
        eventid = id,
        name = title,
        type = type.name,
        description = description,
        location = location ?: Location(0.0, 0.0),
        locationName = locationName,
        time = time.toInstant(ZoneOffset.UTC).toEpochMilli(),
        comments = comments,
        lastUpdated = System.currentTimeMillis(),
    )
