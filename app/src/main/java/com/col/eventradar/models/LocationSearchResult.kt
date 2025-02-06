package com.col.eventradar.models

data class LocationSearchResult(
    val placeId: Long,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val southLat: Double,
    val northLat: Double,
    val westLon: Double,
    val eastLon: Double
)