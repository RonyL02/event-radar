package com.col.eventradar.models

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
        fun fromString(value: String): EventType =
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

        val allExceptUnknown: List<EventType>
            get() = entries.filterNot { it == UNKNOWN }
    }
}
