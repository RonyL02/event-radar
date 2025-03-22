package com.col.eventradar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.col.eventradar.models.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreasOfInterestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: AreaEntity)

    @Query("SELECT * FROM areas")
    fun getAllFeatures(): Flow<List<AreaEntity>>

    @Query("DELETE FROM areas WHERE placeId = :placeId")
    suspend fun deleteFeature(placeId: String)

    @Query("SELECT * FROM areas")
    suspend fun getAllFeaturesNow(): List<AreaEntity>

    @Query("SELECT * FROM areas WHERE country = :country")
    suspend fun getFeaturesByCountry(country: String): List<AreaEntity>

    @Query("DELETE FROM areas")
    suspend fun clearAllFeatures()
}
