package com.col.eventradar.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import com.col.eventradar.models.common.Location
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val locationName: String,
    val locationLat: Double,
    val locationLon: Double,
    val time: Long, // Stored as epoch millis
    val type: String,
    val description: String,
)

fun Event.toEntity() =
    EventEntity(
        id = id,
        title = title,
        locationName = locationName,
        locationLat = location?.latitude ?: 0.0,
        locationLon = location?.longitude ?: 0.0,
        time = time.toInstant(ZoneOffset.UTC).toEpochMilli(),
        type = type.name,
        description = description,
    )

fun EventEntity.toDomain() =
    Event(
        id = id,
        title = title,
        location = Location(locationLat, locationLon),
        locationName = locationName,
        time = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC),
        type = EventType.fromCode(type),
        description = description,
        comments = emptyList(), // Room does not store comments
    )
