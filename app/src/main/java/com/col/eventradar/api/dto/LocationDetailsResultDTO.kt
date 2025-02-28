package com.col.eventradar.api.dto

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

data class LocationDetailsResultDTO(
    val place_id: Long,
    val parent_place_id: Long,
    val osm_type: String,
    val osm_id: Long,
    val category: String,
    val type: String,
    val admin_level: Int,
    val localname: String,
    val names: Names,
    val address: List<FullAddress>,
    val country_code: String,
    val indexed_date: String,
    val importance: Double,
    val calculated_importance: Double,
    val extratags: Extratags,
    val calculated_wikipedia: String,
    val rank_address: Int,
    val rank_search: Int,
    val isarea: Boolean,
    val centroid: Centroid,
    val geometry: Geometry,
    val icon: String
)

data class Names(
    val name: String,
    val name_en: String
)

data class FullAddress(
    val localname: String,
    val place_id: Long,
    val osm_id: Long,
    val osm_type: String,
    val class_: String,
    val type: String,
    val rank_address: Int,
    val distance: Double,
    val isaddress: Boolean
)

data class Extratags(
    val flag: String,
    val sqkm: String,
    val wikidata: String,
    val wikipedia: String,
    val population: String,
    val geonames_id: String,
    val capital_city: String,
    val driving_side: String,
    val wikipedia_en: String,
    val ISO3166_1_alpha2: String,
    val ISO3166_1_alpha3: String,
    val default_language: String,
    val ISO3166_1_numeric: String,
    val country_code_fips: String,
    val country_code_iso3166_1_alpha_2: String
)

data class Centroid(
    val type: String,
    val coordinates: List<Double>
)

data class Geometry(
    val type: String,
    val coordinates: JsonElement
)


