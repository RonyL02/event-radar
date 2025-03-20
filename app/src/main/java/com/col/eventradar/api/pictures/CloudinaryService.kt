package com.col.eventradar.api.cloudinary

import android.content.Context
import android.net.Uri
import android.util.Log
import com.col.eventradar.api.pictures.CloudinaryClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class CloudinaryService(
    private val context: Context,
) {
    suspend fun uploadImageToCloudinary(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = getFileFromUri(uri)
                if (file == null) {
                    Log.e(TAG, "uploadImageToCloudinary: Failed to create file from Uri")
                    return@withContext null
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipartBody =
                    MultipartBody.Part.createFormData("file", file.name, requestFile)

                val uploadPreset = UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = CloudinaryClient.api.uploadImage(multipartBody, uploadPreset)

                if (response.isSuccessful) {
                    response.body()?.secure_url
                } else {
                    Log.e(TAG, "Cloudinary upload failed: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image", e)
                null
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "CloudinaryService"
        private const val UPLOAD_PRESET = "android-event-radar"
    }
}
