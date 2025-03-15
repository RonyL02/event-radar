package com.col.eventradar.models.common

data class Location(
    val latitude: Double = 0.0, // ✅ Default values ensure Firestore can instantiate it
    val longitude: Double = 0.0,
)
