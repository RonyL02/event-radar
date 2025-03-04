package com.col.eventradar.data

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.models.Event
import com.col.eventradar.models.EventType
import com.col.eventradar.models.toDomain
import com.col.eventradar.models.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EventRepository(
    context: Context,
) {
    private val eventDao = AppLocalDatabase.getDatabase(context).eventDao()
    private val gdacsService = GdacsService.getInstance()

    suspend fun fetchAndStoreEvents(
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        alertLevels: List<AlertLevel>? = null,
        eventTypes: List<EventType>? = null,
        country: String? = null,
    ): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking local database for existing events...")

                val localEvents = getLocalEvents()

                val latestStoredEventDate = getLatestStoredEventDate()

                // Determine the actual start date for the API request
                val actualFromDate =
                    latestStoredEventDate?.plusSeconds(1) ?: fromDate
                        ?: LocalDateTime.now().minusYears(1)
                val actualToDate = toDate ?: LocalDateTime.now()

                // If data is already up-to-date, return cached events
                if (latestStoredEventDate != null && latestStoredEventDate.isAfter(actualFromDate)) {
                    Log.d(TAG, "Data up-to-date. Returning only cached events.")
                    return@withContext localEvents
                }

                Log.d(TAG, "Fetching new events from API from $actualFromDate to $actualToDate...")

                // Fetch new events from API
                val response =
                    gdacsService.fetchEvents(
                        actualFromDate,
                        actualToDate,
                        alertLevels,
                        eventTypes,
                        country,
                    )

                val newEvents =
                    if (response != null && response.features.isNotEmpty()) {
                        val events = response.toDomain()

                        val filteredEvents =
                            events.filter { event ->
                                event.title.isNotBlank()
                            }

                        val eventEntities = filteredEvents.map { it.toEntity() }

                        val insertedIds = eventDao.insertEvents(eventEntities)
                        val insertedCount =
                            insertedIds.count { it != -1L } // ✅ Count successful inserts
                        Log.d(TAG, "Successfully inserted $insertedCount events into the database.")

                        insertedIds.map { id ->
                            Log.d(TAG, "Inserted Event ID: $id")
                        }

                        Log.d(TAG, "Fetched and stored ${eventEntities.size} new events.")
                        filteredEvents
                    } else {
                        Log.w(TAG, "No new events found.")
                        emptyList()
                    }

                debugDatabase()
                val uniqueEvents = (newEvents).distinctBy { it.id }
                uniqueEvents.map { event ->
                    Log.d(TAG, "Combined Event: ${event.id}, ${event.title}, ${event.time}")
                }

                localEvents.map { event ->
                    Log.d(TAG, "Local Event: ${event.id}, ${event.title}, ${event.time}")
                }

                // Return combined list of local and new events
                return@withContext newEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events: ${e.localizedMessage}. Returning local DB.", e)
                return@withContext getLocalEvents()
            }
        }
    }

    private suspend fun getLatestStoredEventDate(): LocalDateTime? =
        withContext(Dispatchers.IO) {
            val latestEvent = eventDao.getLatestEvent()
            latestEvent?.time
        }

    suspend fun debugDatabase() {
        val events = eventDao.getAllEvents()
        Log.d(TAG, "Debug DB: Found ${events.size} events stored.")
        events.forEach { Log.d(TAG, "Stored Event: ${it.id}, ${it.title}, ${it.time}") }
    }

    suspend fun getLocalEvents(): List<Event> =
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
