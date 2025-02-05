package com.col.eventradar.network

import com.col.eventradar.network.dto.LocationDetailsResultDTO
import com.col.eventradar.network.dto.LocationSearchResultDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int
    ): List<LocationSearchResultDTO>

    @GET("details")
    suspend fun getLocationDetails(
        @Query("osmtype") osmType: String = "R",
        @Query("osmid") osmId: Long,
        @Query("polygon_geojson") polygonGeoJson: Int = 1,
        @Query("format") format: String = "json"
    ): LocationDetailsResultDTO

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("format") format: String = "json",
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): LocationSearchResultDTO
}
