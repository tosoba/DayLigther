package com.trm.daylighter.core.domain.util.ext

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.SunriseSunset
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.ZoneId
import kotlin.math.abs

fun SunriseSunset.allTimestamps(): List<LocalDateTime?> =
  listOf(
    astronomicalTwilightBegin,
    astronomicalTwilightEnd,
    civilTwilightBegin,
    civilTwilightEnd,
    nauticalTwilightBegin,
    nauticalTwilightEnd,
    sunrise,
    sunset
  )

fun ZoneId.juneSolstice(): LocalDateTime =
  LocalDate.of(Year.now(this).value, Month.JUNE, 22).atStartOfDay()

fun ZoneId.decemberSolstice(): LocalDateTime =
  LocalDate.of(Year.now(this).value, Month.DECEMBER, 22).atStartOfDay()

fun SunriseSunset.isPolarDayAtLocation(location: Location): Boolean {
  if (allTimestamps().any { it != null }) return false

  val now = LocalDateTime.now(location.zoneId)
  val juneSolstice = location.zoneId.juneSolstice()
  val decemberSolstice = location.zoneId.decemberSolstice()
  return if (location.latitude > 0) {
    abs(Duration.between(decemberSolstice, now).seconds) >
      abs(Duration.between(juneSolstice, now).seconds)
  } else {
    abs(Duration.between(decemberSolstice, now).seconds) <
      abs(Duration.between(juneSolstice, now).seconds)
  }
}

fun SunriseSunset.isPolarNightAtLocation(location: Location): Boolean {
  if (allTimestamps().any { it != null }) return false

  val now = LocalDateTime.now(location.zoneId)
  val juneSolstice = location.zoneId.juneSolstice()
  val decemberSolstice = location.zoneId.decemberSolstice()
  return if (location.latitude > 0) {
    abs(Duration.between(decemberSolstice, now).seconds) <
      abs(Duration.between(juneSolstice, now).seconds)
  } else {
    abs(Duration.between(decemberSolstice, now).seconds) >
      abs(Duration.between(juneSolstice, now).seconds)
  }
}
