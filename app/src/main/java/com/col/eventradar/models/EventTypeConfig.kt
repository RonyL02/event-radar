package com.col.eventradar.models

import com.col.eventradar.R

object EventTypeConfig {
    private val configMap =
        mapOf(
            EventType.EARTHQUAKE to
                EventTypeDetails(
                    name = "Disaster",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.FLOOD to
                EventTypeDetails(
                    name = "Suicide",
                    iconResId = R.drawable.pistol,
                ),
            EventType.CYCLONE to
                EventTypeDetails(
                    name = "Suicide",
                    iconResId = R.drawable.pistol,
                ),
            EventType.FOREST_FIRE to
                EventTypeDetails(
                    name = "Suicide",
                    iconResId = R.drawable.pistol,
                ),
            EventType.DROUGHT to
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
