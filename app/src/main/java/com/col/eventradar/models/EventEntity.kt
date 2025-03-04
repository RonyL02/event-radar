package com.col.eventradar.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val locationName: String,
    val time: LocalDateTime,
    val type: String,
    val description: String,
)

fun Event.toEntity() =
    EventEntity(
        id = id,
        title = title,
        locationName = locationName,
        time = time,
        type = type.name,
        description = description,
    )

fun EventEntity.toDomain() =
    Event(
        id = id,
        title = title,
        location = null,
        locationName = locationName,
        time = time,
        type = EventType.valueOf(type),
        description = description,
    )
