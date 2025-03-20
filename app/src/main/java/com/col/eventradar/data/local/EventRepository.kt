package com.col.eventradar.data

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.AreaEntity
import com.col.eventradar.models.AreaOfInterest
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
    private val userRepository = UserRepository(context)

    suspend fun fetchAndStoreEvents(
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        alertLevels: List<AlertLevel>? = null,
        eventTypes: List<EventType>? = null,
    ): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                val loggedInUser = userRepository.getLoggedInUser()
                val countries = loggedInUser?.areasOfInterest?.map { it.country }

                Log.d(TAG, "Checking local database for existing events...")
                val localEvents = getLocalEvents()
                Log.d(TAG, "Retrieved ${localEvents.size} local events.")

                val latestStoredEventDate = getLatestStoredEventDate()
                Log.d(TAG, "Latest stored event date: $latestStoredEventDate")

                val actualFromDate =
                    latestStoredEventDate?.plusSeconds(1) ?: fromDate ?: LocalDateTime
                        .now()
                        .minusYears(1)
                val actualToDate = toDate ?: LocalDateTime.now()

                Log.d(
                    TAG,
                    "Fetching new events from API with range: $actualFromDate - $actualToDate",
                )

                if (latestStoredEventDate != null && latestStoredEventDate.isAfter(actualFromDate)) {
                    Log.d(TAG, "Data up-to-date. Returning only cached events.")
                    return@withContext localEvents
                }

                val response =
                    gdacsService.fetchEvents(
                        actualFromDate,
                        actualToDate,
                        alertLevels,
                        eventTypes,
                        countries,
                    )

                Log.d(TAG, "response: $response")

                val newEvents =
                    if (response.features.isNotEmpty()) {
                        val events = response.toDomain()
                        Log.d(TAG, "Fetched ${events.size} new events from API.")

                        val eventEntities = events.map { it.toEntity() }
                        val insertedIds = eventDao.insertEvents(eventEntities)
                        val insertedCount = insertedIds.count { it != -1L }

                        Log.d(TAG, "Stored $insertedCount new events in local database.")
                        events
                    } else {
                        Log.w(TAG, "No new events found from API.")
                        emptyList()
                    }

                val combinedEvents = (newEvents + localEvents)
                val uniqueEvents =
                    combinedEvents.distinctBy { it.id }.sortedByDescending { it.time }

                Log.d(TAG, "Returning ${uniqueEvents.size} unique events.")
                return@withContext uniqueEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events: ${e.localizedMessage}. Returning local DB.", e)
                return@withContext getLocalEvents()
            }
        }
    }

    suspend fun deleteAreaEvents(area: AreaOfInterest) {
        Log.d(TAG, "Deleting events for area: ${area.country}")
        eventDao.deleteEventsByCountry(area.country)
        Log.d(TAG, "Deleted events for ${area.country}")
    }

    suspend fun addAreaEvents(area: AreaEntity) {
        Log.d(TAG, "Fetching events for new area: ${area.country}")

        val response =
            gdacsService.fetchEvents(
                countries = listOf(area.country),
            )

        val events = response.toDomain()?.map { it.toEntity() }

        if (events.isNullOrEmpty()) {
            Log.d(TAG, "No new events found for area: ${area.country}")
        } else {
            Log.d(
                TAG,
                "Fetched ${events.size} events for area: ${area.country}, inserting into database.",
            )
            eventDao.insertEvents(events)
            Log.d(TAG, "Inserted ${events.size} events for area: ${area.country}")
        }
    }

    private suspend fun getLatestStoredEventDate(): LocalDateTime? =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Fetching latest stored event date from local database.")
            val latestEvent = eventDao.getLatestEvent()
            Log.d(TAG, "Latest stored event date: ${latestEvent?.time}")
            latestEvent?.time
        }

    private suspend fun getLocalEvents(): List<Event> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Fetching all events from local database.")
            val events = eventDao.getAllEvents().map { it.toDomain() }
            Log.d(TAG, "Retrieved ${events.size} events from local database.")
            events
        }

    companion object {
        const val TAG = "EventRepository"
    }
}
