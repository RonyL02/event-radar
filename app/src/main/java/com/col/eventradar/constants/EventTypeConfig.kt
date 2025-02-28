package com.col.eventradar.constants

import com.col.eventradar.R
import com.col.eventradar.models.EventType

object EventTypeConfig {
    private val configMap =
        mapOf(
            EventType.EARTHQUAKE to
                EventTypeDetails(
                    name = "Earthquake",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.FLOOD to
                EventTypeDetails(
                    name = "Flood",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.CYCLONE to
                EventTypeDetails(
                    name = "Cyclone",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.FOREST_FIRE to
                EventTypeDetails(
                    name = "Forest Fire",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.DROUGHT to
                EventTypeDetails(
                    name = "Drought",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.TSUNAMI to
                EventTypeDetails(
                    name = "Tsunami",
                    iconResId = R.drawable.earthquake,
                ),
            EventType.VOLCANO to
                EventTypeDetails(
                    name = "Volcano",
                    iconResId = R.drawable.earthquake,
                ),
        )

    fun getName(eventType: EventType): String = configMap[eventType]?.name ?: "Unknown"

    fun getIconResId(eventType: EventType): Int = configMap[eventType]?.iconResId ?: R.drawable.earthquake
}

data class EventTypeDetails(
    val name: String,
    val iconResId: Int,
)
