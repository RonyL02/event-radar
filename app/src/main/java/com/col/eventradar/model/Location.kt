package com.col.eventradar.model

data class Location(
    val lat: Double,
    val long: Double,
) {
    override fun toString(): String = "($lat, $long)"
}
