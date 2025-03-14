package com.col.eventradar.api.events.dto

import android.text.TextUtils.split
import com.col.eventradar.models.Event
import com.col.eventradar.models.EventType
import com.col.eventradar.models.Location
import com.col.eventradar.utils.toLocalDateTime
import com.google.gson.annotations.SerializedName

data class EventListResponseDTO(
    @SerializedName("features") val features: List<EventFeature>,
)

data class EventFeature(
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("properties") val properties: EventProperties,
)

data class Geometry(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<Double>,
)

data class EventProperties(
    @SerializedName("eventtype") val eventType: String,
    @SerializedName("eventid") val eventId: Long,
    @SerializedName("eventname") val eventName: String?,
    @SerializedName("description") val description: String,
    @SerializedName("alertlevel") val alertLevel: String,
    @SerializedName("alertscore") val alertScore: Double?,
    @SerializedName("fromdate") val fromDate: String,
    @SerializedName("todate") val toDate: String,
    @SerializedName("country") val country: String,
    @SerializedName("iconoverall") val iconOverall: String,
    @SerializedName("url") val urls: Urls,
    @SerializedName("severitydata") val severityData: SeverityData?,
    @SerializedName("affectedcountries") val affectedCountries: List<AffectedCountry>?,
)

data class Urls(
    @SerializedName("details") val details: String,
)

data class SeverityData(
    @SerializedName("severitytext") val severityText: String?,
    @SerializedName("severity") val severity: Double?,
    @SerializedName("severityunit") val severityUnit: String?,
)

data class AffectedCountry(
    @SerializedName("iso2") val iso2: String,
    @SerializedName("iso3") val iso3: String,
    @SerializedName("countryname") val countryName: String,
)

fun EventListResponseDTO.toDomain(): List<Event> =
    features.map { feature ->
        val titleName =
            if (feature.properties.eventName.isNullOrBlank()) {
                feature.properties.description
                    .split(" ")
                    .firstOrNull() ?: "Unknown Event"
            } else {
                feature.properties.eventName
            }

        Event(
            id = feature.properties.eventId.toString(),
            title = titleName,
            location = feature.geometry.toLocation(),
            locationName =
                feature.properties.affectedCountries
                    ?.firstOrNull()
                    ?.countryName
                    ?: "Unknown Location",
            time = feature.properties.fromDate.toLocalDateTime(),
            type = EventType.fromString(feature.properties.eventType),
            description = feature.properties.description,
        )
    }

fun Geometry.toLocation(): Location = Location(latitude = coordinates[1], longitude = coordinates[0])
