package com.col.eventradar.models;

public enum EventType {
    Disaster,
    Suicide,
}

enum class EventTypes(val code: String) {
    EARTHQUAKE("EQ"),
    TSUNAMI("TS"),
    FLOOD("FL"),
    CYCLONE("TC"),
    VOLCANO("VO"),
    DROUGHT("DR"),
    FOREST_FIRE("WF");

    override fun toString(): String = code
}
