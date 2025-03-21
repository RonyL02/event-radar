package com.col.eventradar.api.locations

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenStreetMapService {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    private val client =
        OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", "Event-Radar/1.0")
                        .build()
                chain.proceed(request)
            }.build()
    val api: LocationApi by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(LocationApi::class.java)
    }
}
