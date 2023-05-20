package com.trm.daylighter.core.domain.model

import com.trm.daylighter.core.domain.serializer.LocalDateSerializer
import com.trm.daylighter.core.domain.serializer.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunset(
  @Serializable(with = LocalDateTimeSerializer::class) val astronomicalTwilightBegin: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val astronomicalTwilightEnd: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val civilTwilightBegin: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val civilTwilightEnd: LocalDateTime,
  val dayLengthSeconds: Int,
  @Serializable(with = LocalDateTimeSerializer::class) val nauticalTwilightBegin: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val nauticalTwilightEnd: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val solarNoon: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val sunrise: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class) val sunset: LocalDateTime,
  @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
)
