package com.col.eventradar.data.remote.events

import android.util.Log
import com.col.eventradar.data.remote.events.dto.EventListResponseDTO
import com.col.eventradar.models.AlertLevel
import com.col.eventradar.models.EventType
import java.time.LocalDateTime

class EventRepository {
    suspend fun fetchEvents(
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        alertLevels: List<AlertLevel>?,
        eventTypes: List<EventType>?,
        country: String?,
    ): EventListResponseDTO? {
        val queryParams =
            GdacsQueryHelper.buildEventQueryParams(
                fromDate = fromDate ?: LocalDateTime.now().minusDays(7),
                toDate = toDate ?: LocalDateTime.now(),
                alertLevels =
                    alertLevels ?: listOf(
                        AlertLevel.RED,
                        AlertLevel.ORANGE,
                        AlertLevel.GREEN,
                    ),
                eventTypes = eventTypes ?: EventType.allExceptUnknown,
                country = country ?: "United States",
            )

        Log.d("EventRepository", "Query params: $queryParams")

        return try {
            val response =
                GdacsService.api.getEventList(
                    fromDate = queryParams[GdacsQueryParams.FROM_DATE] ?: return null,
                    toDate = queryParams[GdacsQueryParams.TO_DATE] ?: return null,
                    alertLevel = queryParams[GdacsQueryParams.ALERT_LEVEL] ?: return null,
                    eventList = queryParams[GdacsQueryParams.EVENT_LIST] ?: return null,
                    country = queryParams[GdacsQueryParams.COUNTRY] ?: return null,
                )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.features.isNotEmpty()) {
                    body
                } else {
                    Log.w("EventRepository", "No events found" + response.message())
                    null
                }
            } else {
                Log.e("EventRepository", "API Error: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Network error fetching events", e)
            null
        }
    }
}
