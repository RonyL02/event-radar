package com.col.eventradar.models.common

import com.col.eventradar.models.common.Event.Companion.DESCRIPTION_PREVIEW_LENGTH
import com.col.eventradar.models.common.Event.Companion.TITLE_PREVIEW_LENGTH
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

    companion object {
        const val DESCRIPTION_PREVIEW_LENGTH = 50
        const val TITLE_PREVIEW_LENGTH = 14
    }
}

fun Event.getDescriptionPreview() =
    if (description.length > DESCRIPTION_PREVIEW_LENGTH) {
        description.take(DESCRIPTION_PREVIEW_LENGTH) + "..."
    } else {
        description
    }

fun Event.getTitlePreview() =
    if (title.length > TITLE_PREVIEW_LENGTH) {
        title.take(TITLE_PREVIEW_LENGTH) + "..."
    } else {
        title
    }
