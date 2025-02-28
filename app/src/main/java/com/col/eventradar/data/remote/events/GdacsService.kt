package com.col.eventradar.network.events

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GdacsService {
    private const val BASE_URL = "https://www.gdacs.org/gdacsapi/api/events/"

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

    val api: GdacsApi by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GdacsApi::class.java)
    }
}
