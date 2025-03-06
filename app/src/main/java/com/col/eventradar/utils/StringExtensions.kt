package com.col.eventradar.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
