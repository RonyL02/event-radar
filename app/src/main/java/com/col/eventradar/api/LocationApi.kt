package com.col.eventradar.api

import com.col.eventradar.api.dto.LocationDetailsResultDTO
import com.col.eventradar.api.dto.LocationSearchResultDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") lang: String = "en",
        @Query("limit") limit: Int
    ): List<LocationSearchResultDTO>

    @GET("details")
    suspend fun getLocationDetails(
        @Query("place_id") placeId: Long,
        @Query("polygon_geojson") polygonGeoJson: Int = 1,
        @Query("format") format: String = "json",
        @Query("accept-language") lang: String = "en"
    ): LocationDetailsResultDTO

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("format") format: String = "json",
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("zoom") zoom: Int = 14,
        @Query("accept-language") lang: String = "en"
    ): LocationSearchResultDTO
}
