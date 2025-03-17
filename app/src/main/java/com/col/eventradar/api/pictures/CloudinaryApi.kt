package com.col.eventradar.api.cloudinary

import com.col.eventradar.api.pictures.dto.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryApi {
    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part, // The image file
        @Part("upload_preset") uploadPreset: RequestBody, // ✅ Cloudinary Upload Preset
    ): Response<CloudinaryResponse>
}
