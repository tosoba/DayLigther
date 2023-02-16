package com.trm.daylighter.feature.day.ext

import com.trm.daylighter.core.domain.model.SunriseSunset
import java.time.ZonedDateTime

internal fun SunriseSunset.getUpcomingTimestampsSorted(now: ZonedDateTime): List<ZonedDateTime> =
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
    .filter { it.isAfter(now) }
    .sorted()

internal fun SunriseSunset.now(): ZonedDateTime = ZonedDateTime.now(sunrise.zone)
