package com.col.eventradar.models

data class LocationSearchResult(
    val osmId: Double,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val southLat: Double,
    val northLat: Double,
    val westLon: Double,
    val eastLon: Double
)