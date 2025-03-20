package com.col.eventradar.api.pictures

import com.col.eventradar.api.cloudinary.CloudinaryApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CloudinaryClient {
    private const val BASE_URL = "https://api.cloudinary.com/v1_1/dk2deadpe/"

    private val client =
        OkHttpClient.Builder().build()

    val api: CloudinaryApi by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(CloudinaryApi::class.java)
    }
}
