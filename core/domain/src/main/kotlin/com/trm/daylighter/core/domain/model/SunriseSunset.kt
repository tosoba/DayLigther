package com.trm.daylighter.core.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class SunriseSunset(
  val astronomicalTwilightBegin: ZonedDateTime,
  val astronomicalTwilightEnd: ZonedDateTime,
  val civilTwilightBegin: ZonedDateTime,
  val civilTwilightEnd: ZonedDateTime,
  val dayLengthSeconds: Int,
  val nauticalTwilightBegin: ZonedDateTime,
  val nauticalTwilightEnd: ZonedDateTime,
  val solarNoon: ZonedDateTime,
  val sunrise: ZonedDateTime,
  val sunset: ZonedDateTime,
  val date: LocalDate,
)
