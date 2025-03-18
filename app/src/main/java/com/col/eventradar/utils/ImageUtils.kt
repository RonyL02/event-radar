package com.col.eventradar.utils

import android.content.ContentResolver
import android.media.ExifInterface
import android.net.Uri
import android.widget.ImageView
import android.widget.ProgressBar
import com.col.eventradar.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImageUtils {
    fun loadImage(
        imageUri: Uri?,
        imageView: ImageView,
        defaultImageRes: Int = R.mipmap.ic_launcher_round,
        width: Int = 100,
        height: Int = 100,
    ) {
        Picasso
            .get()
            .load(imageUri)
            .resize(width, height)
            .centerCrop()
            .placeholder(defaultImageRes)
            .error(defaultImageRes)
            .into(imageView)
    }

    fun loadImageWithOnLoad(
        imageUri: Uri?,
        imageView: ImageView,
        onLoadComplete: () -> Unit,
        defaultImageRes: Int = R.mipmap.ic_launcher_round,
        width: Int = 300,
        height: Int = 200,
    ) {
        Picasso
            .get()
            .load(imageUri)
            .resize(width, height)
            .centerCrop()
            .placeholder(defaultImageRes)
            .error(defaultImageRes)
            .into(
                imageView,
                object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        onLoadComplete()
                    }

                    override fun onError(e: Exception?) {
                        onLoadComplete()
                    }
                },
            )
    }

    fun showImgInViewFromGallery(
        contentResolver: ContentResolver,
        imageView: ImageView,
        imageUri: Uri,
        defaultImageRes: Int = R.mipmap.ic_launcher_round,
        width: Int = 100,
        height: Int = 100,
    ) {
        val inputStream = contentResolver.openInputStream(imageUri)
        if (inputStream != null) {
            val exif = ExifInterface(inputStream)
            val rotation =
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )

            val degrees =
                when (rotation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                    else -> 0F
                }

            inputStream.close()

            Picasso
                .get()
                .load(imageUri.toString())
                .rotate(degrees)
                .resize(width, height)
                .centerCrop()
                .placeholder(defaultImageRes)
                .error(defaultImageRes)
                .into(imageView)
        } else {
            imageView.setImageResource(defaultImageRes)
        }
    }

    fun showImgInViewFromUrl(
        imageUri: String,
        imageView: ImageView,
        progressBar: ProgressBar,
        defaultImageRes: Int = R.mipmap.ic_launcher,
        width: Int = 300,
        height: Int = 300,
    ) {
        progressBar.visibility = ProgressBar.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Picasso
                    .get()
                    .load(imageUri)
                    .resize(width, height)
                    .centerCrop()
                    .error(defaultImageRes)
                    .into(imageView)

                progressBar.visibility = ProgressBar.GONE
            } catch (e: Exception) {
                progressBar.visibility = ProgressBar.GONE
                imageView.setImageResource(defaultImageRes)
            }
        }
    }
}
