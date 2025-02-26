package com.col.eventradar.utils

import java.time.LocalDateTime
import java.util.Locale

fun LocalDateTime.getFormattedTime() = String.format(Locale.UK, "%02d:%02d", this.hour, this.minute)
