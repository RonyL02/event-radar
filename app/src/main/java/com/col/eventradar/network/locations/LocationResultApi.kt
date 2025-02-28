package com.col.eventradar.network.locations

import com.col.eventradar.network.locations.dto.LocationSearchResultDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationResultApi {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int,
    ): List<LocationSearchResultDTO>
}
