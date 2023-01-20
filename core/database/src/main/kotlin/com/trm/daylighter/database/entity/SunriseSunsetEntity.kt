package com.trm.daylighter.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity(tableName = "sunrise_sunset")
data class SunriseSunsetEntity(
  @ColumnInfo(name = "astronomical_twilight_begin") val astronomicalTwilightBegin: ZonedDateTime,
  @ColumnInfo(name = "astronomical_twilight_end") val astronomicalTwilightEnd: ZonedDateTime,
  @ColumnInfo(name = "civil_twilight_begin") val civilTwilightBegin: ZonedDateTime,
  @ColumnInfo(name = "civil_twilight_end") val civilTwilightEnd: ZonedDateTime,
  @ColumnInfo(name = "day_length_seconds") val dayLengthSeconds: Int,
  @ColumnInfo(name = "nautical_twilight_begin") val nauticalTwilightBegin: ZonedDateTime,
  @ColumnInfo(name = "nautical_twilight_end") val nauticalTwilightEnd: ZonedDateTime,
  @ColumnInfo(name = "solar_noon") val solarNoon: ZonedDateTime,
  val sunrise: ZonedDateTime,
  val sunset: ZonedDateTime,
  @PrimaryKey val date: LocalDate,
)