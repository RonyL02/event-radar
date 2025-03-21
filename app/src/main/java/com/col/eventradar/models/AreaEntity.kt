package com.col.eventradar.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.maplibre.geojson.Feature

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey val placeId: String,
    val name: String,
    val country: String,
    val geojson: String,
)

fun AreaEntity.toFeature(): Feature = Feature.fromJson(this.geojson)
