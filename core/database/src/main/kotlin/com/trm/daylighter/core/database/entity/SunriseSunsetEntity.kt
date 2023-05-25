package com.trm.daylighter.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
  tableName = "sunrise_sunset",
  primaryKeys = ["location_id", "date"],
  foreignKeys =
    [
      ForeignKey(
        entity = LocationEntity::class,
        parentColumns = ["id"],
        childColumns = ["location_id"],
        onDelete = ForeignKey.CASCADE
      )
    ]
)
data class SunriseSunsetEntity(
  @ColumnInfo(name = "astronomical_twilight_begin") val astronomicalTwilightBegin: LocalDateTime?,
  @ColumnInfo(name = "astronomical_twilight_end") val astronomicalTwilightEnd: LocalDateTime?,
  @ColumnInfo(name = "civil_twilight_begin") val civilTwilightBegin: LocalDateTime?,
  @ColumnInfo(name = "civil_twilight_end") val civilTwilightEnd: LocalDateTime?,
  @ColumnInfo(name = "day_length_seconds") val dayLengthSeconds: Int,
  @ColumnInfo(name = "nautical_twilight_begin") val nauticalTwilightBegin: LocalDateTime?,
  @ColumnInfo(name = "nautical_twilight_end") val nauticalTwilightEnd: LocalDateTime?,
  val sunrise: LocalDateTime?,
  val sunset: LocalDateTime?,
  val date: LocalDate,
  @ColumnInfo(name = "location_id") val locationId: Long,
)
