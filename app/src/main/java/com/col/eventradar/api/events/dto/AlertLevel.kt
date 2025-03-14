package com.col.eventradar.api.events.dto

enum class AlertLevel(
    val value: String,
) {
    GREEN("green"),
    ORANGE("orange"),
    RED("red"),
    ;

    override fun toString(): String = value
}
