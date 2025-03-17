package com.col.eventradar.data.repository

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.data.local.AppLocalDatabase
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import com.col.eventradar.models.local.toDomain
import com.col.eventradar.models.local.toEntity
import com.col.eventradar.models.remote.CommentFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EventRepository(
    context: Context,
) {
    private val eventDao = AppLocalDatabase.getDatabase(context).eventDao()
    private val gdacsService = GdacsService.getInstance()
    private val firestore = FirebaseFirestore.getInstance() // ✅ Firestore instance
    private val commentsCollection =
        firestore.collection(COMMENTS_COLLECTION) // ✅ Firestore comments collection

    /**
     * ✅ Fetch events **only from GDACS API**, store **only in Room**, then fetch **comments** from Firestore.
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
                Log.d(TAG, "Checking local Room database for existing events...")

                // 🔥 Step 1: Get local events from Room DB
                val localEvents = getLocalEvents()

                // 🔥 Step 2: Get the latest stored event timestamp
                val latestStoredEventDate = getLatestStoredEventDate()

                val actualFromDate =
                    latestStoredEventDate?.plusSeconds(1) ?: fromDate ?: LocalDateTime
                        .now()
                        .minusYears(1)
                val actualToDate = toDate ?: LocalDateTime.now()

                if (latestStoredEventDate != null && latestStoredEventDate.isAfter(actualFromDate)) {
                    Log.d(TAG, "Data up-to-date. Returning only cached events.")
                    return@withContext updateEventComments(localEvents)
                }

                Log.d(
                    TAG,
                    "Fetching new events from GDACS API from $actualFromDate to $actualToDate...",
                )

                // 🌍 Step 3: Fetch from GDACS API
                val response =
                    gdacsService.fetchEvents(
                        actualFromDate,
                        actualToDate,
                        alertLevels,
                        eventTypes,
                        country,
                    )

                val newEvents = response?.toDomain() ?: emptyList()

                if (newEvents.isEmpty()) {
                    Log.w(TAG, "No new events found.")
                    return@withContext updateEventComments(localEvents)
                }

                // 💾 Step 4: Store only new events in Room Database
                val eventEntities = newEvents.map { it.toEntity() }
                eventDao.insertEvents(eventEntities)

                val combinedEvents =
                    (newEvents + localEvents)
                        .distinctBy { it.id }
                        .sortedByDescending { it.time }

                Log.d(TAG, "Final event count after merge: ${combinedEvents.size}")

                // ✅ Step 5: Fetch comments from Firestore
                return@withContext updateEventComments(combinedEvents)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events: ${e.localizedMessage}. Returning local DB.", e)
                return@withContext updateEventComments(getLocalEvents())
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

    /**
     * ✅ Get the latest stored event timestamp from Room.
     */
    private suspend fun getLatestStoredEventDate(): LocalDateTime? =
        withContext(Dispatchers.IO) {
            val latestEvent = eventDao.getLatestEvent()?.toDomain()
            latestEvent?.time
        }

    /**
     * ✅ Fetch **all comments** for multiple events from Firestore.
     */
    private suspend fun fetchCommentsForEvents(eventIds: List<String>): Map<String, List<Comment>> =
        withContext(Dispatchers.IO) {
            try {
                val eventComments = mutableMapOf<String, List<Comment>>()

                for (eventId in eventIds) {
                    val snapshot =
                        commentsCollection
                            .whereEqualTo("eventId", eventId)
                            .get()
                            .await()

                    val comments =
                        snapshot.documents.mapNotNull {
                            it.toObject(CommentFirestore::class.java)?.toDomain()
                        }

                    eventComments[eventId] = comments
                }

                Log.d(TAG, "Fetched comments: $eventComments")
                return@withContext eventComments
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comments from Firestore", e)
                return@withContext emptyMap()
            }
        }

    /**
     * ✅ Attach the fetched comments to each event before returning.
     */
    private suspend fun updateEventComments(events: List<Event>): List<Event> {
        val eventIds = events.map { it.id }
        val commentsMap = fetchCommentsForEvents(eventIds)

        return events.map { event ->
            event.copy(comments = commentsMap[event.id] ?: emptyList())
        }
    }

    companion object {
        const val TAG = "EventRepository"
        const val COMMENTS_COLLECTION = "comments"
    }
}
