package com.col.eventradar.network.locations

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
                        .header("User-Agent", "Event-Radar/1.0 (your.email@example.com)")
                        .build()
                chain.proceed(request)
            }.build()
    val api: LocationResultApi by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(LocationResultApi::class.java)
    }
}
