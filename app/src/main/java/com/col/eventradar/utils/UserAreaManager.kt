package com.col.eventradar.utils

import MapUtils
import com.col.eventradar.data.EventRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.AreaEntity
import com.col.eventradar.models.AreaOfInterest
import com.col.eventradar.models.User
import com.col.eventradar.models.toFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.Style
import org.maplibre.geojson.FeatureCollection

class UserAreaManager(private val userRepository: UserRepository, private val eventRepository: EventRepository, private val style: Style?) {
    suspend fun getUser(userId: String): User? {
        return userRepository.getUser(userId)
    }

    suspend fun addAreaOfInterest(userId: String, area: AreaEntity) {
        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            area,
            UserRepository.UpdateOperations.ArrayUnion
        )

        eventRepository.addAreaEvents(area)

        withContext(Dispatchers.Main) {
            style?.let {
                MapUtils.setSourceFeatures(
                    it,
                    MapUtils.AREAS_OF_INTEREST_SOURCE_NAME,
                    FeatureCollection.fromFeatures(listOf(area.toFeature())) // TODO: Add and not replace
                )
            }
        }
    }

    suspend fun removeAreaOfInterest(userId: String, area: AreaOfInterest) {
        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            area,
            UserRepository.UpdateOperations.ArrayRemove
        )

        eventRepository.deleteAreaEvents(area)

        withContext(Dispatchers.Main) {
            style?.let {
                MapUtils.setSourceFeatures(
                    it,
                    MapUtils.AREAS_OF_INTEREST_SOURCE_NAME,
                    FeatureCollection.fromFeatures(listOf()) // TODO: remove and not replace
                )
            }
        }
    }
}
