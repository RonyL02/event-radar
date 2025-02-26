package com.col.eventradar.models

import java.time.LocalDateTime

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

    fun getDescriptionPreview() =
        if (description.length > DESCRIPTION_PREVIEW_LENGTH) {
            description.take(DESCRIPTION_PREVIEW_LENGTH) + "..."
        } else {
            description
        }

    companion object {
        const val DESCRIPTION_PREVIEW_LENGTH = 50
    }
}
