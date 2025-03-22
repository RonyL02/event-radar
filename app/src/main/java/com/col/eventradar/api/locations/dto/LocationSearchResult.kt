package com.col.eventradar.api.locations.dto

data class LocationSearchResult(
    val placeId: Long,
    val osmId: Long,
    val osmType: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val southLat: Double,
    val northLat: Double,
    val westLon: Double,
    val eastLon: Double,
)
