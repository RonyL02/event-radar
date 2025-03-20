package com.col.eventradar.utils

import com.col.eventradar.data.EventRepository
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.AreaOfInterest
import com.col.eventradar.models.User
import com.col.eventradar.models.toAreaEntity

class UserAreaManager(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val areaOfInterestRepository: AreasOfInterestRepository,
) {
    suspend fun getUser(userId: String): User? = userRepository.getUser(userId)

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
        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            area,
            UserRepository.UpdateOperations.ArrayRemove,
        )

        eventRepository.deleteAreaEvents(area)

        areaOfInterestRepository.deleteFeature(area.placeId)
    }
}
