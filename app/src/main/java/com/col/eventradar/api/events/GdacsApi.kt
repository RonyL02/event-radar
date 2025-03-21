package com.col.eventradar.api.events

import com.col.eventradar.api.events.dto.EventListResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GdacsApi {
    @GET("geteventlist/SEARCH")
    suspend fun getEventList(
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String,
        @Query("alertlevel") alertLevel: String = "orange;red;green",
        @Query("eventlist") eventList: String = "EQ,TS,TC,FL,VO,DR,WF",
        @Query("country") country: String = "",
    ): Response<EventListResponseDTO>
}
