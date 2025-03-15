package com.col.eventradar.data.local

import android.content.Context
import android.util.Log
import com.col.eventradar.api.events.GdacsService
import com.col.eventradar.api.events.dto.AlertLevel
import com.col.eventradar.api.events.dto.toDomain
import com.col.eventradar.models.common.Comment
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.EventType
import com.col.eventradar.models.local.toDomain
import com.col.eventradar.models.local.toEntity
import com.col.eventradar.models.remote.EventFirestore
import com.col.eventradar.models.remote.toDomain
import com.col.eventradar.models.remote.toFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.reflect.KProperty1

class EventRepository(
    context: Context,
) {
    private val firestore = FirebaseFirestore.getInstance() // ✅ Firestore instance
    private val eventsCollection =
        firestore.collection(EVENTS_COLLECTION) // ✅ Firestore events collection
    private val eventDao = AppLocalDatabase.getDatabase(context).eventDao()
    private val commentDao = AppLocalDatabase.getDatabase(context).commentDao()
    private val gdacsService = GdacsService.getInstance()

    /**
     * ✅ Fetch events first from Firestore, then API if needed.
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
                Log.d(TAG, "Checking Firestore for existing events...")

                // 🔥 Step 1: Get events from Firestore
                val firestoreEvents = getEventsFromFirestore()

                Log.d(TAG, "Firestore Events: ${firestoreEvents.size}")

                // 🔥 Step 2: Get local events from Room DB
                val localEvents = getLocalEvents()

                // 🔥 Step 3: Get latest stored event timestamp
                val latestStoredEventDate = getLatestStoredEventDate()

                val actualFromDate =
                    latestStoredEventDate?.plusSeconds(1) ?: fromDate ?: LocalDateTime
                        .now()
                        .minusYears(1)
                val actualToDate = toDate ?: LocalDateTime.now()

                if (latestStoredEventDate != null && latestStoredEventDate.isAfter(actualFromDate)) {
                    Log.d(TAG, "Data up-to-date. Returning only cached events.")
                    return@withContext (firestoreEvents + localEvents).distinctBy { it.id }
                }

                Log.d(TAG, "Fetching new events from API from $actualFromDate to $actualToDate...")

                // 🌍 Step 4: Fetch from GDACS API
                val response =
                    gdacsService.fetchEvents(
                        actualFromDate,
                        actualToDate,
                        alertLevels,
                        eventTypes,
                        country,
                    )

                val newEvents =
                    response?.toDomain() ?: emptyList()

                // ✅ Step 5: Filter out duplicates before adding to Firestore
                val existingEventIds = firestoreEvents.map { it.id }.toSet()
                val eventsToAdd = newEvents.filter { it.id !in existingEventIds }

                // 🔥 Step 6: Add new events to Firestore (with empty comments)
                eventsToAdd.forEach { event ->
                    eventsCollection.document(event.id).set(event.toFirestore()).await()
                }

                // 💾 Step 7: Store all events in Room Database
                val eventEntities = (firestoreEvents + newEvents).map { it.toEntity() }
                eventDao.insertEvents(eventEntities)

                val combinedEvents =
                    (firestoreEvents + newEvents + localEvents)
                        .distinctBy { it.id }
                        .sortedByDescending { it.time }
                Log.d(TAG, "Final event count: ${combinedEvents.size}")

                return@withContext combinedEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events: ${e.localizedMessage}. Returning local DB.", e)
                return@withContext getLocalEvents()
            }
        }
    }

    /**
     * ✅ Get latest events from Firestore.
     */
    private suspend fun getEventsFromFirestore(): List<Event> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = eventsCollection.get().await()
                snapshot.toObjects(EventFirestore::class.java).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events from Firestore", e)
                emptyList()
            }
        }

    /**
     * ✅ Get latest stored event timestamp.
     */
    private suspend fun getLatestStoredEventDate(): LocalDateTime? =
        withContext(Dispatchers.IO) {
            val latestEvent = eventDao.getLatestEvent()
            latestEvent?.time?.let { epochMillis ->
                LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
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

    suspend fun getCommentsFromFirestore(eventId: String): List<Comment> =
        try {
            val eventSnapshot =
                eventsCollection.document(eventId).get().await() // 🔥 Get event document

            val event = eventSnapshot.toObject(EventFirestore::class.java) // Convert to object

            event?.comments ?: emptyList() // ✅ Return comments array (or empty list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching comments for event: $eventId", e)
            emptyList() // Return empty list if there's an error
        }

    suspend fun addCommentToEvent(
        eventId: String,
        newComment: Comment,
    ) {
        Log.d(TAG, "addCommentToEvent: $eventId , $newComment")
        updateEventField(
            eventId = eventId,
            field = EventFirestore::comments,
            value = newComment.toFirestore(),
            operation = UpdateOperations.ArrayUnion,
        )

        // 💾 Save to Room Database
        commentDao.insertComment(newComment.toEntity(eventId))
    }

    suspend fun syncCommentsFromFirestore(eventId: String) {
        val comments = getCommentsFromFirestore(eventId) // ✅ Get comments from Firestore

        if (comments.isNotEmpty()) {
            commentDao.deleteCommentsForEvent(eventId) // ✅ Clear old local comments
            commentDao.insertComments(comments.map { it.toEntity(eventId) }) // ✅ Save new comments to Room
        }
    }

    /**
     * 💾 Fetch Comments from Local Database (Room)
     */
    suspend fun getLocalComments(eventId: String): List<Comment> = commentDao.getCommentsForEvent(eventId).map { it.toDomain() }

    suspend fun <T : Any> updateEventField(
        eventId: String,
        field: KProperty1<EventFirestore, T>,
        value: T,
        operation: UpdateOperations = UpdateOperations.Set,
    ) {
        val fieldName = field.name

        val updateValue: Any =
            when (operation) {
                UpdateOperations.ArrayUnion -> FieldValue.arrayUnion(value)
                UpdateOperations.ArrayRemove -> FieldValue.arrayRemove(value)
                else -> value
            }

        try {
            eventsCollection
                .document(eventId)
                .update(fieldName, updateValue)
                .await()

            Log.d(TAG, "Successfully updated $fieldName in event $eventId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event field: $fieldName in $eventId", e)
        }
    }

    companion object {
        const val EVENTS_COLLECTION = "events"
        const val TAG = "EventRepository"
    }

    enum class UpdateOperations {
        Set,
        ArrayUnion,
        ArrayRemove,
    }
}
