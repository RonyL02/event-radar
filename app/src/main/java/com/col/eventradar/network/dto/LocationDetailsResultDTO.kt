package com.col.eventradar.network.dto

import org.maplibre.geojson.Feature
import org.maplibre.geojson.MultiPolygon
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
) {
        fun toMapLibreFeature(): Feature {
            // Check the geometry type
            val geometry = this.geometry
            val feature = when (geometry.type) {
                "Polygon" -> {
                    // Create a Polygon geometry using the coordinates
                    val coordinates = geometry.coordinates[0] // Assuming first array is the outer ring
                    val polygonCoordinates = coordinates.map { point ->
                        Point.fromLngLat(point[0], point[1]) // Convert to Point using longitude and latitude values
                    }
                    val polygon = Polygon.fromLngLats(listOf(polygonCoordinates)) // Wrap in a list of coordinates
                    Feature.fromGeometry(polygon)
                }
                else -> {
                    // Fallback to creating a point if geometry type is neither Polygon nor MultiPolygon
                    val centroidCoordinates = this.centroid.coordinates
                    val point = Point.fromLngLat(centroidCoordinates[0], centroidCoordinates[1])
                    Feature.fromGeometry(point)
                }
            }

            // Add properties to the feature
            feature.apply {
                addStringProperty("osm_id", osm_id.toString())
                addStringProperty("localname", localname)
                addStringProperty("category", category)
                addStringProperty("type", type)
                addStringProperty("country_code", country_code)
            }

            return feature
        }
    }

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
    val coordinates: List<List<List<Double>>>
)
