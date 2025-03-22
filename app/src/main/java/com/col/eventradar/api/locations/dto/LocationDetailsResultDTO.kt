package com.col.eventradar.api.locations.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LocationDetailsResultDTO(
    @SerializedName("place_id") val placeId: Long,
    @SerializedName("parent_place_id") val parentPlaceId: Long,
    @SerializedName("osm_type") val osmType: String,
    @SerializedName("osm_id") val osmId: Long,
    val category: String,
    val type: String,
    val localname: String,
    val names: Names,
    val address: List<FullAddress>,
    @SerializedName("country_code") val country: String,
    val extratags: Extratags,
    val calculatedWikipedia: String,
    val isarea: Boolean,
    val centroid: Centroid,
    val geometry: Geometry,
    val icon: String,
)

data class Names(
    val name: String,
    val nameEn: String,
)

data class FullAddress(
    val localname: String,
    val placeId: Long,
    val osmId: Long,
    val osmType: String,
    val class_: String,
    val type: String,
    val distance: Double,
    val isaddress: Boolean,
    val country: String
)

data class Extratags(
    val flag: String,
    val sqkm: String,
    val wikidata: String,
    val wikipedia: String,
    val population: String,
    val geonamesId: String,
    val capitalCity: String,
    val wikipediaEn: String,
)

data class Centroid(
    val type: String,
    val coordinates: List<Double>,
)

data class Geometry(
    val type: String,
    val coordinates: JsonElement,
)
