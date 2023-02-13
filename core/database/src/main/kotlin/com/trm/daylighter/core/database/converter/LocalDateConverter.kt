package com.trm.daylighter.core.database.converter

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object LocalDateConverter {
  @TypeConverter
  fun fromTimestamp(value: String): LocalDate =
    DateTimeFormatter.ISO_DATE.parse(value, LocalDate::from)

  @TypeConverter
  fun dateToTimestamp(value: LocalDate): String = DateTimeFormatter.ISO_DATE.format(value)
}
