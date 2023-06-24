package com.trm.daylighter.core.common.util.ext

import com.trm.daylighter.core.common.model.DayMode
import com.trm.daylighter.core.common.model.DayPeriod
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.util.ext.decemberSolstice
import com.trm.daylighter.core.domain.util.ext.juneSolstice
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

fun SunriseSunset.currentPeriodIn(location: Location): DayPeriod {
  val now = LocalDateTime.now(location.zoneId)
  return when {
    now.isBeforeOtherNotNull(astronomicalTwilightBegin) ||
      now.isEqualOrAfterOtherNotNull(astronomicalTwilightEnd) -> {
      DayPeriod.NIGHT
    }
    now.isInPeriod(
      beginMorning = astronomicalTwilightBegin,
      endMorning = nauticalTwilightBegin,
      beginEvening = nauticalTwilightEnd,
      endEvening = astronomicalTwilightEnd
    ) -> {
      DayPeriod.ASTRONOMICAL
    }
    now.isInPeriod(
      beginMorning = nauticalTwilightBegin,
      endMorning = civilTwilightBegin,
      beginEvening = civilTwilightEnd,
      endEvening = nauticalTwilightEnd
    ) -> {
      DayPeriod.NAUTICAL
    }
    now.isInPeriod(
      beginMorning = civilTwilightBegin,
      endMorning = sunrise,
      beginEvening = sunset,
      endEvening = civilTwilightEnd
    ) -> {
      DayPeriod.CIVIL
    }
    now.isEqualOrAfterOtherNotNull(sunrise) && now.isBeforeOtherNotNull(sunset) -> {
      DayPeriod.DAY
    }
    else -> {
      val juneSolstice = location.zoneId.juneSolstice()
      val decemberSolstice = location.zoneId.decemberSolstice()
      if (
        abs(Duration.between(decemberSolstice, now).seconds) <
          abs(Duration.between(juneSolstice, now).seconds)
      ) {
        if (location.latitude > 0) DayPeriod.NIGHT else DayPeriod.DAY
      } else {
        if (location.latitude > 0) DayPeriod.DAY else DayPeriod.NIGHT
      }
    }
  }
}

fun SunriseSunset.dayPeriodStartTime(dayPeriod: DayPeriod, dayMode: DayMode): LocalTime {
  val nightStart = LocalTime.ofSecondOfDay(1L)
  val noon = LocalTime.NOON
  return when (dayPeriod) {
    DayPeriod.NIGHT -> {
      when (dayMode) {
        DayMode.SUNRISE -> nightStart
        DayMode.SUNSET -> astronomicalTwilightEnd?.toLocalTime() ?: noon
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> astronomicalTwilightBegin?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> nauticalTwilightEnd?.toLocalTime() ?: noon
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> nauticalTwilightBegin?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> civilTwilightEnd?.toLocalTime() ?: noon
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> civilTwilightBegin?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> sunset?.toLocalTime() ?: noon
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunrise?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> noon
      }
    }
  }
}

fun SunriseSunset.dayPeriodEndTime(dayPeriod: DayPeriod, dayMode: DayMode): LocalTime {
  val nightEnd = LocalTime.MAX
  val noon = LocalTime.NOON
  return when (dayPeriod) {
    DayPeriod.NIGHT -> {
      when (dayMode) {
        DayMode.SUNRISE -> astronomicalTwilightBegin?.toLocalTime() ?: noon
        DayMode.SUNSET -> nightEnd
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> nauticalTwilightBegin?.toLocalTime() ?: noon
        DayMode.SUNSET -> astronomicalTwilightEnd?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> civilTwilightBegin?.toLocalTime() ?: noon
        DayMode.SUNSET -> nauticalTwilightEnd?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunrise?.toLocalTime() ?: noon
        DayMode.SUNSET -> civilTwilightEnd?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> noon
        DayMode.SUNSET -> sunset?.toLocalTime() ?: nightEnd
      }
    }
  }
}

private fun LocalDateTime.isInPeriod(
  beginMorning: LocalDateTime?,
  endMorning: LocalDateTime?,
  beginEvening: LocalDateTime?,
  endEvening: LocalDateTime?
): Boolean =
  (isEqualOrAfterOtherNotNull(beginMorning) && isBeforeOtherOrOtherIsNull(endMorning)) ||
    (beginMorning == null && isBeforeOtherNotNull(endMorning)) ||
    (isEqualOrAfterOtherNotNull(beginEvening) && isBeforeOtherOrOtherIsNull(endEvening)) ||
    (beginEvening == null && isBeforeOtherNotNull(endEvening))
