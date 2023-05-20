package com.trm.daylighter.core.database.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeConverter {
  @TypeConverter
  fun fromTimestamp(value: String): LocalDateTime =
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(value, LocalDateTime::from)

  @TypeConverter
  fun dateToTimestamp(value: LocalDateTime): String =
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value)
}
