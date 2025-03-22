package com.col.eventradar.data.repository

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.AreaEntity
import com.col.eventradar.models.common.AreaOfInterest
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
    private val userRepository by lazy { UserRepository(context) }

    /**
     * Fetch events **only from GDACS API**, store **only in Room**, then fetch **comments** from Firestore.
     */
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

                Log.d(
                    TAG,
                    "Checking local database for existing events...",
                )
                val localEvents = getLocalEvents()
                Log.d(
                    TAG,
                    "Retrieved ${localEvents.size} local events.",
                )

                val latestStoredEventDate = getLatestStoredEventDate()
                Log.d(
                    TAG,
                    "Latest stored event date: $latestStoredEventDate",
                )

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
                    Log.d(
                        TAG,
                        "Data up-to-date. Returning only cached events.",
                    )
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
                        Log.d(
                            TAG,
                            "Fetched ${events.size} new events from API.",
                        )

                        val eventEntities = events.map { it.toEntity() }
                        val insertedIds = eventDao.insertEvents(eventEntities)
                        val insertedCount = insertedIds.count { it != -1L }

                        Log.d(
                            TAG,
                            "Stored $insertedCount new events in local database.",
                        )
                        events
                    } else {
                        Log.w(
                            TAG,
                            "No new events found from API.",
                        )
                        emptyList()
                    }

                val combinedEvents = (newEvents + localEvents)
                val uniqueEvents =
                    combinedEvents.distinctBy { it.id }.sortedByDescending { it.time }

                Log.d(
                    TAG,
                    "Returning ${uniqueEvents.size} unique events.",
                )
                return@withContext updateEventComments(uniqueEvents)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error fetching events: ${e.localizedMessage}. Returning local DB.",
                    e,
                )
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

        val events = response.toDomain().map { it.toEntity() }

        if (events.isEmpty()) {
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

    suspend fun getLocalEventById(eventId: String): Event? =
        withContext(Dispatchers.IO) {
            return@withContext eventDao.getEventById(eventId)?.toDomain()
        }

    suspend fun clearAllEvents() {
        withContext(Dispatchers.IO) {
            eventDao.deleteAllEvents()
        }
    }
    suspend fun deleteLocalEventsLeftovers() {
        withContext(Dispatchers.IO) {
            try {
                val user = userRepository.getLoggedInUser()

                // Check if user is null
                if (user == null) {
                    Log.e(TAG, "Failed to retrieve logged-in user.")
                    return@withContext
                }

                val interests =
                    user.areasOfInterest.map { it.name.orEmpty().lowercase() }.orEmpty()

                // Log if interests are empty
                if (interests.isEmpty()) {
                    Log.w(TAG, "User has no areas of interest to filter events.")
                }

                // Get all local events and determine which countries are not in the interest list
                val localEvents = eventDao.getAllEvents()

                // Check if there are any local events
                if (localEvents.isEmpty()) {
                    Log.w(TAG, "No local events found to delete.")
                }

                val countriesToDelete =
                    localEvents
                        .mapNotNull { it.locationName.lowercase() } // Null check for locationName
                        .filter { it.isNotBlank() && it !in interests }
                        .distinct()

                // Log the countries to delete
                if (countriesToDelete.isNotEmpty()) {
                    Log.d(
                        TAG,
                        "Deleting events from countries: ${countriesToDelete.joinToString()}",
                    )
                }

                // Delete events from countries that are no longer in the interest list
                countriesToDelete.forEach { countryName ->
                    // Only log deletion count once
                    val deletedCount = eventDao.deleteEventsByCountry(countryName)
                    if (deletedCount > 0) {
                        Log.d(TAG, "Deleted $deletedCount events for country: $countryName")
                    }
                }

                Log.d(
                    TAG,
                    "deleteLocalEventsLeftovers: Deleted events for user ${user.id} with ${interests.size} interests",
                )

                // Get and log all local events after the deletion
                val remainingEvents = eventDao.getAllEvents()

                if (remainingEvents.isEmpty()) {
                    Log.d(TAG, "No local events remaining after deletion.")
                } else {
                    Log.d(
                        TAG,
                        "Remaining local events after deletion: ${remainingEvents.joinToString()}",
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteLocalEventsLeftovers: Failed to delete events", e)
            }
        }
    }

    companion object {
        const val TAG = "EventRepository"
    }
}
