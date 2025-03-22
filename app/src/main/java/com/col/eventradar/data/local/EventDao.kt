package com.col.eventradar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.col.eventradar.models.local.EventEntity

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events ORDER BY time DESC LIMIT 1")
    suspend fun getLatestEvent(): EventEntity?

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("DELETE FROM events WHERE TRIM(LOWER(locationName)) = TRIM(LOWER(:locationName))")
    suspend fun deleteEventsByCountry(locationName: String): Int
}
