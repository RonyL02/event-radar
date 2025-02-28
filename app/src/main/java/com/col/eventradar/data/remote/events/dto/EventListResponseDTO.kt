package com.col.eventradar.network.events.dto

import com.col.eventradar.models.Event
import com.col.eventradar.models.EventType
import com.col.eventradar.models.Location
import com.col.eventradar.utils.toLocalDateTime

data class EventListResponseDTO(
    val features: List<EventFeature>,
)

data class EventFeature(
    val geometry: Geometry,
    val properties: EventProperties,
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>, // [longitude, latitude]
)

data class EventProperties(
    val eventtype: String,
    val eventid: Long,
    val eventname: String?,
    val description: String,
    val alertlevel: String,
    val alertscore: Double?,
    val fromdate: String,
    val todate: String,
    val country: String,
    val iconoverall: String,
    val url: Urls,
    val severitydata: SeverityData?,
    val affectedcountries: List<AffectedCountry>?,
)

data class Urls(
    val details: String,
)

data class SeverityData(
    val severitytext: String?,
    val severity: Double?,
    val severityunit: String?,
)

fun EventListResponseDTO.toDomain(): List<Event> =
    features.map { feature ->
        Event(
            id = feature.properties.eventid.toString(),
            title = feature.properties.eventname.takeIf { !it.isNullOrBlank() } ?: "Unknown Event",
            location = feature.geometry.toLocation(),
            locationName = feature.properties.country,
            time = feature.properties.fromdate.toLocalDateTime(),
            type = EventType.fromString(feature.properties.eventtype),
            description = feature.properties.description,
        )
    }

fun Geometry.toLocation(): Location = Location(latitude = coordinates[1], longitude = coordinates[0])
