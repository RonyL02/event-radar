package com.col.eventradar.models

import com.col.eventradar.R
import java.time.LocalDateTime
import java.util.Locale

/**
 * A placeholder item representing a piece of content.
 */
data class Event(
    val id: String,
    val title: String,
    val location: Location?,
    val locationName: String,
    val time: LocalDateTime,
    val type: EventType,
    val description: String,
    val comments: List<Comment> = emptyList(),
) {
    override fun toString(): String = description

    fun getIconRes() =
        when (type) {
            EventType.EarthQuake -> R.drawable.earthquake
            else -> R.drawable.earthquake
        }

    fun getDescriptionPreview() =
        if (description.length > DESCRIPTION_PREVIEW_LENGTH) {
            description.take(DESCRIPTION_PREVIEW_LENGTH) + "..."
        } else {
            description
        }

    fun getFormattedTime() = String.format(Locale.UK, "%02d:%02d", time.hour, time.minute)

    companion object {
        const val DESCRIPTION_PREVIEW_LENGTH = 50
    }
}
