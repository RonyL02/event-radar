package com.col.eventradar.utils

import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.models.AreaOfInterest
import com.col.eventradar.models.User

class UserAreaManager(private val userRepository: UserRepository) {
    suspend fun addAreaOfInterest(userId: String, area: AreaOfInterest) {
        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            area,
            UserRepository.UpdateOperations.ArrayUnion
        )
    }

    suspend fun removeAreaOfInterest(userId: String, area: AreaOfInterest) {
        userRepository.updateUserField(
            userId,
            User::areasOfInterest,
            area,
            UserRepository.UpdateOperations.ArrayRemove
        )
    }
}
