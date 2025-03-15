package com.col.eventradar.data.local

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
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
    private val eventDao = AppLocalDatabase.getDatabase(context).eventDao()
    private val gdacsService = GdacsService.getInstance()

    /**
     * ✅ Fetch events **only from GDACS API** and store **only in Room**
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
                Log.d(TAG, "Fetching events from GDACS API...")

                // 🌍 Fetch from GDACS API
                val response =
                    gdacsService.fetchEvents(
                        fromDate ?: LocalDateTime.now().minusYears(1),
                        toDate ?: LocalDateTime.now(),
                        alertLevels,
                        eventTypes,
                        country,
                    )

                val newEvents = response?.toDomain() ?: emptyList()

                // 💾 Store all events in Room Database (avoid duplicates)
                val eventEntities = newEvents.map { it.toEntity() }
                eventDao.insertEvents(eventEntities)

                val storedEvents = getLocalEvents()
                Log.d(TAG, "Final event count: ${storedEvents.size}")

                return@withContext storedEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events from GDACS API. Returning local DB.", e)
                return@withContext getLocalEvents()
            }
        }
    }

    /**
     * ✅ Get events from local Room database.
     */
    private suspend fun getLocalEvents(): List<Event> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Fetching events from local database.")
            val events = eventDao.getAllEvents().map { it.toDomain() }
            Log.d(TAG, "Retrieved ${events.size} events from local database.")
            events
        }

    companion object {
        const val TAG = "EventRepository"
    }
}
