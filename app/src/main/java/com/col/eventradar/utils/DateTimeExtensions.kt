package com.col.eventradar.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.getFormattedTime() = String.format(Locale.UK, "%02d:%02d", this.hour, this.minute)

fun LocalDateTime.getFormattedDate() = this.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
