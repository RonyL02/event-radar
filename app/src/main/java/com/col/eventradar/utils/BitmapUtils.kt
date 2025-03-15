package com.col.eventradar.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.col.eventradar.constants.EventTypeConfig
import com.col.eventradar.models.EventType
import org.maplibre.android.maps.Style

fun addEventIconsToMap(style: Style, context: Context) {
    EventType.entries.forEach { eventType ->
        val iconResId = EventTypeConfig.getIconResId(eventType)
        val drawable = ContextCompat.getDrawable(context, iconResId)
        val bitmap = drawable?.let { convertDrawableToBitmap(it) }

        bitmap?.let {
            style.addImage(eventType.name, it)
        }
    }
}

fun convertDrawableToBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
