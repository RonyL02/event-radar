package com.col.eventradar.api.locations

import com.col.eventradar.api.locations.dto.LocationDetailsResultDTO
import com.col.eventradar.api.locations.dto.LocationSearchResultDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("accept-language") lang: String = "en",
        @Query("limit") limit: Int = 50,
        @Query("extratags") extraTags: Int = 1,
        @Query("class") classType: String = "boundary",
        @Query("type") type: String = "administrative",
    ): List<LocationSearchResultDTO>

    @GET("details")
    suspend fun getLocationDetails(
        @Query("osmid") osmId: Long,
        @Query("osmtype") osmType: String,
        @Query("polygon_geojson") polygonGeoJson: Int = 1,
        @Query("format") format: String = "json",
        @Query("accept-language") lang: String = "en",
    ): LocationDetailsResultDTO

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("format") format: String = "json",
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("zoom") zoom: Int = 1,
        @Query("accept-language") lang: String = "en",
    ): LocationSearchResultDTO
}
