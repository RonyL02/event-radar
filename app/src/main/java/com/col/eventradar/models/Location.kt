package com.col.eventradar.models

data class Location(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String = "($latitude, $longitude)"
}
