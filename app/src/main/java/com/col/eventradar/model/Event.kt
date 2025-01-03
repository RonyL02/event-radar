package com.col.eventradar.model

/**
 * A placeholder item representing a piece of content.
 */
data class Event(
    val id: String,
    val title: String,
    val content: String,
    val location: Location?,
    val city: String,
    val date: String,
    val type: String,
    val details: String,
) {
    override fun toString(): String = content
}
