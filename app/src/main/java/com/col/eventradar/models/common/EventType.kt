package com.col.eventradar.models.common

enum class EventType(
    val code: String,
) {
    EARTHQUAKE("EQ"),
    TSUNAMI("TS"),
    FLOOD("FL"),
    CYCLONE("TC"),
    VOLCANO("VO"),
    DROUGHT("DR"),
    FOREST_FIRE("WF"),
    UNKNOWN("XX"),
    ;

    override fun toString(): String = code

    companion object {
        fun fromCode(value: String): EventType =
            when (value.uppercase()) {
                "EQ" -> EARTHQUAKE
                "TS" -> TSUNAMI
                "FL" -> FLOOD
                "TC" -> CYCLONE
                "VO" -> VOLCANO
                "DR" -> DROUGHT
                "WF" -> FOREST_FIRE
                else -> UNKNOWN
            }

        fun fromString(value: String): EventType =
            when (value.uppercase()) {
                "EARTHQUAKE" -> EARTHQUAKE
                "TSUNAMI" -> TSUNAMI
                "FLOOD" -> FLOOD
                "CYCLONE" -> CYCLONE
                "VOLCANO" -> VOLCANO
                "DROUGHT" -> DROUGHT
                "FOREST_FIRE" -> FOREST_FIRE
                else -> UNKNOWN
            }

        val allExceptUnknown: List<EventType>
            get() = entries.filterNot { it == UNKNOWN }
    }
}
