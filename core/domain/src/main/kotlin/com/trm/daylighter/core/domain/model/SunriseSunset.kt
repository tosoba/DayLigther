package com.trm.daylighter.core.domain.model

import com.trm.daylighter.core.domain.serializer.LocalDateSerializer
import com.trm.daylighter.core.domain.serializer.ZonedDateTimeSerializer
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunset(
  @Serializable(with = ZonedDateTimeSerializer::class) val astronomicalTwilightBegin: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val astronomicalTwilightEnd: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val civilTwilightBegin: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val civilTwilightEnd: ZonedDateTime,
  val dayLengthSeconds: Int,
  @Serializable(with = ZonedDateTimeSerializer::class) val nauticalTwilightBegin: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val nauticalTwilightEnd: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val solarNoon: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val sunrise: ZonedDateTime,
  @Serializable(with = ZonedDateTimeSerializer::class) val sunset: ZonedDateTime,
  @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
)
