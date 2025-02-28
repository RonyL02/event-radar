package com.col.eventradar.api.events

import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.models.EventType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GdacsQueryHelper {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun buildEventQueryParams(
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        alertLevels: List<AlertLevel>? = listOf(AlertLevel.ORANGE, AlertLevel.RED),
        eventTypes: List<EventType>? = EventType.entries,
        country: String? = "United States",
    ): Map<String, String> =
        mutableMapOf<String, String>().apply {
            fromDate?.let { put(GdacsQueryParams.FROM_DATE, it.format(dateFormatter)) }
            toDate?.let { put(GdacsQueryParams.TO_DATE, it.format(dateFormatter)) }
            alertLevels
                ?.takeIf { it.isNotEmpty() }
                ?.let { put(GdacsQueryParams.ALERT_LEVEL, it.joinToString(";") { it.value }) }
            eventTypes
                ?.takeIf { it.isNotEmpty() }
                ?.let { put(GdacsQueryParams.EVENT_LIST, it.joinToString(",") { it.code }) }
            country?.takeIf { it.isNotBlank() }?.let { put(GdacsQueryParams.COUNTRY, it) }
        }
}
