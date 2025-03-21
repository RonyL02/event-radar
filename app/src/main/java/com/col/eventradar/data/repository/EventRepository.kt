package com.col.eventradar.data.repository

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import com.col.eventradar.models.local.toDomain
import com.col.eventradar.models.local.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EventRepository(
    context: Context,
) {
    private val eventDao by lazy { AppLocalDatabase.getDatabase(context).eventDao() }
    private val gdacsService by lazy { GdacsService.getInstance() }
    private val commentsRepository by lazy { CommentsRepository(context) }

    /**
     * Fetch events **only from GDACS API**, store **only in Room**, then fetch **comments** from Firestore.
     */
    suspend fun fetchAndStoreEvents(
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        alertLevels: List<AlertLevel>? = null,
        eventTypes: List<EventType>? = null,
        country: String? = null,
    ): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                var finalEvents: List<Event> = emptyList()

                val localEvents = getLocalEvents()
                val latestStoredEventDate = getLatestStoredEventDate()

                val actualFromDate =
                    latestStoredEventDate?.plusSeconds(1) ?: fromDate ?: LocalDateTime
                        .now()
                        .minusYears(1)
                val actualToDate = toDate ?: LocalDateTime.now()

                if (latestStoredEventDate != null && latestStoredEventDate.isAfter(actualFromDate)) {
                    return@withContext updateEventComments(localEvents)
                }

                val response =
                    gdacsService.fetchEvents(
                        actualFromDate,
                        actualToDate,
                        alertLevels,
                        eventTypes,
                        listOf(country ?: ""),
                    )
                val newEvents = response.toDomain()

                if (newEvents.isEmpty()) {
                    return@withContext updateEventComments(localEvents)
                }

                val eventEntities = newEvents.map { it.toEntity() }
                eventDao.insertEvents(eventEntities)

                val combinedEvents =
                    (newEvents + localEvents)
                        .distinctBy { it.id }
                        .sortedByDescending { it.time }

                finalEvents = updateEventComments(combinedEvents)

                return@withContext finalEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events: ${e.localizedMessage}. Returning local DB.", e)
                return@withContext updateEventComments(getLocalEvents())
            }
        }
    }

    /**
     * Get events from local Room database.
     */
    private suspend fun getLocalEvents(): List<Event> =
        withContext(Dispatchers.IO) {
            eventDao.getAllEvents().map { it.toDomain() }
        }

    /**
     * Get the latest stored event timestamp from Room.
     */
    private suspend fun getLatestStoredEventDate(): LocalDateTime? =
        withContext(Dispatchers.IO) {
            eventDao.getLatestEvent()?.toDomain()?.time
        }

    /**
     * Attach the fetched comments to each event before returning.
     */
    private suspend fun updateEventComments(events: List<Event>): List<Event> {
        val eventIds = events.map { it.id }
        val commentsMap = commentsRepository.getCommentsForEvents(eventIds)

        return events.map { event ->
            event.copy(comments = commentsMap[event.id] ?: emptyList())
        }
    }

    suspend fun getLocalEventById(eventId: String): Event? =
        withContext(Dispatchers.IO) {
            return@withContext eventDao.getEventById(eventId)?.toDomain()
        }

    companion object {
        const val TAG = "EventRepository"
    }
}
