package com.trm.daylighter.core.domain.model

import com.trm.daylighter.core.domain.serializer.LocalDateSerializer
import com.trm.daylighter.core.domain.serializer.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunset(
  @Serializable(with = LocalDateTimeSerializer::class)
  val astronomicalTwilightBegin: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val astronomicalTwilightEnd: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val civilTwilightBegin: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val civilTwilightEnd: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val nauticalTwilightBegin: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val nauticalTwilightEnd: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val sunrise: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val sunset: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val goldenHourAboveMorning: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val goldenHourBelowMorning: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val goldenHourAboveEvening: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val goldenHourBelowEvening: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val blueHourBegin: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val blueHourEnd: LocalDateTime?,
  @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
)
