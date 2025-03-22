package com.col.eventradar.api.locations.dto

import com.google.gson.annotations.SerializedName

data class LocationSearchResultDTO(
    @SerializedName("place_id") val placeId: Long,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("address") val address: Address?,
    @SerializedName("name") val name: String?,
    @SerializedName("boundingbox") val boundingBox: List<String>?,
    @SerializedName("class") val className: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("extratags") val extraTags: Map<String, String>?,
    @SerializedName("osm_type") val osmType: String,
    @SerializedName("osm_id") val osmId: Long,
)

data class Address(
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?,
)

fun LocationSearchResultDTO.toModel(): LocationSearchResult {
    val southLat = boundingBox?.getOrNull(0)?.toDoubleOrNull() ?: latitude
    val northLat = boundingBox?.getOrNull(1)?.toDoubleOrNull() ?: latitude
    val westLon = boundingBox?.getOrNull(2)?.toDoubleOrNull() ?: longitude
    val eastLon = boundingBox?.getOrNull(3)?.toDoubleOrNull() ?: longitude

    return LocationSearchResult(
        placeId = placeId,
        name = name ?: "",
        country = address?.country ?: "",
        latitude = latitude,
        longitude = longitude,
        southLat = southLat,
        northLat = northLat,
        westLon = westLon,
        eastLon = eastLon,
        osmType = osmType,
        osmId = osmId
    )
}
