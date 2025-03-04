package com.col.eventradar.data.local

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeConverter {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String = value?.format(formatter) ?: ""

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = if (value.isNullOrBlank()) null else LocalDateTime.parse(value, formatter)
}
