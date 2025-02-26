package com.col.eventradar.models

import com.col.eventradar.R

object EventTypeConfig {
    private val configMap =
        mapOf(
            EventType.Disaster to
                EventTypeDetails(
                    name = "Disaster",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.Suicide to
                EventTypeDetails(
                    name = "Suicide",
                    iconResId = R.drawable.pistol,
                ),
        )

    fun getName(eventType: EventType): String = configMap[eventType]?.name ?: "Unknown"

    fun getIconResId(eventType: EventType): Int = configMap[eventType]?.iconResId ?: R.drawable.earthquake
}

data class EventTypeDetails(
    val name: String,
    val iconResId: Int,
)
