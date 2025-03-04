package com.col.eventradar.api.events

import android.util.Log
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.EventListResponseDTO
import com.col.eventradar.models.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class GdacsService private constructor() {
    suspend fun fetchEvents(
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        alertLevels: List<AlertLevel>?,
        eventTypes: List<EventType>?,
        country: String?,
    ): EventListResponseDTO? =
        coroutineScope {
            val start = fromDate ?: LocalDateTime.now().minusYears(1)
            val end = toDate ?: LocalDateTime.now()

            // Split date range into 20 smaller ranges
            val dateRanges = splitDateRange(start, end, REQUEST_BATCHES_AMOUNT)

            // Run 20 API calls in parallel
            val deferredResults =
                dateRanges.map { range ->
                    async(Dispatchers.IO) {
                        fetchSingleEventBatch(
                            range.first,
                            range.second,
                            alertLevels,
                            eventTypes,
                            country,
                        )
                    }
                }

            // Wait for all parallel requests to finish
            val responses = deferredResults.awaitAll().filterNotNull()

            dateRanges.map { range ->
                Log.d("GdacsService", "Fetched events from ${range.first} to ${range.second}")
            }

            // Combine results
            val combinedEvents = responses.flatMap { it.features }
            val unique = combinedEvents.distinctBy { it.properties.eventId }
            Log.d("GdacsService", "Fetched unique events: ${unique.size}")
            Log.d("GdacsService", "Fetched total events: ${combinedEvents.size}")

            return@coroutineScope if (combinedEvents.isNotEmpty()) {
                EventListResponseDTO(features = combinedEvents)
            } else {
                null
            }
        }

    private suspend fun fetchSingleEventBatch(
        fromDate: LocalDateTime,
        toDate: LocalDateTime,
        alertLevels: List<AlertLevel>?,
        eventTypes: List<EventType>?,
        country: String?,
    ): EventListResponseDTO? {
        val queryParams =
            GdacsQueryHelper.buildEventQueryParams(
                fromDate = fromDate,
                toDate = toDate,
                alertLevels =
                    alertLevels ?: listOf(
                        AlertLevel.RED,
                        AlertLevel.ORANGE,
                        AlertLevel.GREEN,
                    ),
                eventTypes = eventTypes ?: EventType.allExceptUnknown,
                country = country ?: "United States",
            )

        return try {
            val response =
                GdacsClient.api.getEventList(
                    fromDate = queryParams[GdacsQueryParams.FROM_DATE] ?: return null,
                    toDate = queryParams[GdacsQueryParams.TO_DATE] ?: return null,
                    alertLevel = queryParams[GdacsQueryParams.ALERT_LEVEL] ?: return null,
                    eventList = queryParams[GdacsQueryParams.EVENT_LIST] ?: return null,
                    country = queryParams[GdacsQueryParams.COUNTRY] ?: return null,
                )

            if (response.isSuccessful) {
                response.body()?.takeIf { it.features.isNotEmpty() }
            } else {
                Log.e("GdacsService", "API Error: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("GdacsService", "Network error fetching events: ${e.localizedMessage}")
            null
        }
    }

    private fun splitDateRange(
        start: LocalDateTime,
        end: LocalDateTime,
        parts: Int,
    ): List<Pair<LocalDateTime, LocalDateTime>> {
        val totalMinutes = ChronoUnit.MINUTES.between(start, end)
        val stepMinutes = totalMinutes / parts

        return (0 until parts).map { i ->
            val rangeStart = start.plusMinutes(i * stepMinutes)
            val rangeEnd = if (i == parts - 1) end else rangeStart.plusMinutes(stepMinutes)
            Pair(rangeStart, rangeEnd)
        }
    }

    companion object {
        private const val REQUEST_BATCHES_AMOUNT = 5

        @Volatile
        private var instance: GdacsService? = null

        fun getInstance(): GdacsService =
            instance ?: synchronized(this) {
                instance ?: GdacsService().also { instance = it }
            }
    }
}
