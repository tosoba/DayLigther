package com.trm.daylighter.core.database.converter

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ZonedDateTimeConverter {
  @TypeConverter
  fun fromTimestamp(value: String): ZonedDateTime =
    DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(value, ZonedDateTime::from)

  @TypeConverter
  fun dateToTimestamp(value: ZonedDateTime): String =
    DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value)
}
