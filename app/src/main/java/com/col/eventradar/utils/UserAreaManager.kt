package com.col.eventradar.utils

import android.util.Log
import com.col.eventradar.data.EventRepository
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.common.AreaOfInterest
import com.col.eventradar.models.common.User
import com.col.eventradar.models.common.toAreaEntity

class UserAreaManager(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val areaOfInterestRepository: AreasOfInterestRepository,
) {
    suspend fun getUser(userId: String): User? = userRepository.getUserById(userId)

    suspend fun addAreaOfInterest(
        userId: String,
        areaOfInterest: AreaOfInterest,
        geojson: String,
    ) {
        val area = areaOfInterest.toAreaEntity(geojson)
        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            areaOfInterest,
            UserRepository.UpdateOperations.ArrayUnion,
        )

        eventRepository.addAreaEvents(area)
    }

    suspend fun removeAreaOfInterest(
        userId: String,
        area: AreaOfInterest,
    ) {
        Log.d("AreaRemoval", "🔁 Removing area ${area.name} for user $userId")

        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            area,
            UserRepository.UpdateOperations.ArrayRemove,
        )
        Log.d("AreaRemoval", "✅ Removed area from user document in Firestore")

        eventRepository.deleteAreaEvents(area)
        Log.d("AreaRemoval", "🗑️ Deleted events related to area ${area.name}")

        areaOfInterestRepository.deleteFeature(area.placeId)
        Log.d("AreaRemoval", "🧹 Deleted map feature with placeId ${area.placeId}")

        Log.d("AreaRemoval", "🎯 Finished removing area ${area.name}")
    }
}
