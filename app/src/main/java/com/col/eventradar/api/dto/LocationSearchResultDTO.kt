package com.col.eventradar.api.dto

import com.col.eventradar.models.LocationSearchResult
import com.google.gson.annotations.SerializedName

data class LocationSearchResultDTO(
    @SerializedName("place_id") val placeId: Long,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("address") val address: Address?,
    @SerializedName("name") val name: String?,
    @SerializedName("boundingbox") val boundingBox: List<String>?
)

data class Address(
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?
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
        eastLon = eastLon
    )
}
