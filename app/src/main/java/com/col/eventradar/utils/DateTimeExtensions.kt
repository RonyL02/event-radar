package com.col.eventradar.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun LocalDateTime.getFormattedTime() = String.format(Locale.UK, "%02d:%02d", this.hour, this.minute)

fun LocalDateTime.getFormattedDate(): String = this.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

fun LocalDateTime.getShortFormattedDate(): String = this.format(DateTimeFormatter.ofPattern("dd/MM/yy"))

fun LocalDateTime.getTimeAgo(): String {
    val now = LocalDateTime.now()
    val minutesDiff = ChronoUnit.MINUTES.between(this, now)
    val daysDiff = ChronoUnit.DAYS.between(this.toLocalDate(), now.toLocalDate())

    return when {
        minutesDiff < 1 -> "Just now"
        minutesDiff < 60 -> "$minutesDiff minutes ago"
        daysDiff == 0L -> this.format(DateTimeFormatter.ofPattern("HH:mm"))
        daysDiff == 1L -> "Yesterday at ${this.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> this.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    }
}
