package com.col.eventradar.data.local

import android.content.Context
import com.col.eventradar.models.AreaEntity
import org.maplibre.geojson.Feature

class AreasOfInterestRepository(
    context: Context,
) {
    private val areasDao = AppLocalDatabase.getDatabase(context).areasDao()

    val storedFeaturesFlow = areasDao.getAllFeatures()

    suspend fun saveFeature(
        feature: Feature,
        geojson: String,
    ) {
        areasDao.insertFeature(
            AreaEntity(
                feature.getStringProperty("placeId"),
                feature.getStringProperty("localname"),
                feature.getStringProperty("localname"),
                geojson,
            ),
        )
    }

    suspend fun deleteFeature(placeId: String) {
        areasDao.deleteFeature(placeId)
    }

    suspend fun clearAll() {
        areasDao.clearAllFeatures()
    }

    companion object {
        private const val TAG = "AreasOfInterestRepository"
    }
}
