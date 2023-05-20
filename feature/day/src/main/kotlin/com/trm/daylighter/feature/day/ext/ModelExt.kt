package com.trm.daylighter.feature.day.ext

import androidx.compose.ui.graphics.Color
import com.trm.daylighter.core.common.util.ext.isEqualOrAfter
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.feature.day.model.DayPeriod
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun SunriseSunset.getUpcomingTimestampsSorted(now: LocalDateTime): List<LocalDateTime> =
  allTimestamps().filter { it.isAfter(now) }.sorted()

internal fun SunriseSunset.allTimestamps(): List<LocalDateTime> =
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

internal fun SunriseSunset.currentPeriod(zoneId: ZoneId): DayPeriod {
  val now = now(zoneId)
  return when {
    now.isBefore(astronomicalTwilightBegin) || now.isEqualOrAfter(astronomicalTwilightEnd) -> {
      DayPeriod.NIGHT
    }
    (now.isEqualOrAfter(astronomicalTwilightBegin) && now.isBefore(nauticalTwilightBegin)) ||
      (now.isEqualOrAfter(nauticalTwilightEnd) && now.isBefore(astronomicalTwilightEnd)) -> {
      DayPeriod.ASTRONOMICAL
    }
    (now.isEqualOrAfter(nauticalTwilightBegin) && now.isBefore(civilTwilightBegin)) ||
      (now.isEqualOrAfter(civilTwilightEnd) && now.isBefore(nauticalTwilightEnd)) -> {
      DayPeriod.NAUTICAL
    }
    (now.isEqualOrAfter(civilTwilightBegin) && now.isBefore(sunrise)) ||
      (now.isEqualOrAfter(sunset) && now.isBefore(civilTwilightEnd)) -> {
      DayPeriod.CIVIL
    }
    now.isEqualOrAfter(sunrise) && now.isBefore(sunset) -> {
      DayPeriod.DAY
    }
    else -> {
      DayPeriod.DAY
    }
  }
}

internal fun DayPeriod.color(): Color =
  when (this) {
    DayPeriod.NIGHT -> nightColor
    DayPeriod.ASTRONOMICAL -> astronomicalTwilightColor
    DayPeriod.NAUTICAL -> nauticalTwilightColor
    DayPeriod.CIVIL -> civilTwilightColor
    DayPeriod.DAY -> dayColor
  }

internal fun DayPeriod.textColor(): Color = if (this == DayPeriod.DAY) Color.Black else Color.White

internal fun DayPeriod.textShadowColor(): Color =
  if (this == DayPeriod.DAY) Color.White else Color.Black

internal fun SunriseSunset.now(zoneId: ZoneId): LocalDateTime =
  ZonedDateTime.now(zoneId).toLocalDateTime()
