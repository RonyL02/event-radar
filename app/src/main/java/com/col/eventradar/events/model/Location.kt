package com.col.eventradar.events.model

data class Location(
    val lat: Double,
    val long: Double,
) {
    override fun toString(): String = "($lat, $long)"
}
