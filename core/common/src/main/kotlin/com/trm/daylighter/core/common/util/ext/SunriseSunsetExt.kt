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

fun SunriseSunset.currentPeriodIn(location: Location, useGoldenBlueHour: Boolean): DayPeriod {
  val now = LocalDateTime.now(location.zoneId)
  return when {
    now.isBeforeOtherNotNull(morning18Below) || now.isEqualOrAfterOtherNotNull(evening18Below) -> {
      DayPeriod.NIGHT
    }
    now.isInPeriod(
      beginMorning = morning18Below,
      endMorning = morning12Below,
      beginEvening = evening12Below,
      endEvening = evening18Below
    ) -> {
      DayPeriod.ASTRONOMICAL
    }
    now.isInPeriod(
      beginMorning = morning12Below,
      endMorning = morning6Below,
      beginEvening = evening6Below,
      endEvening = evening12Below
    ) -> {
      DayPeriod.NAUTICAL
    }
    !useGoldenBlueHour &&
      now.isInPeriod(
        beginMorning = morning6Below,
        endMorning = sunrise,
        beginEvening = sunset,
        endEvening = evening6Below
      ) -> {
      DayPeriod.CIVIL
    }
    useGoldenBlueHour &&
      now.isInPeriod(
        beginMorning = morning4Below,
        endMorning = morning6Above,
        beginEvening = evening6Above,
        endEvening = evening4Below
      ) -> {
      DayPeriod.GOLDEN_HOUR
    }
    useGoldenBlueHour &&
      now.isInPeriod(
        beginMorning = morning6Below,
        endMorning = morning4Below,
        beginEvening = evening4Below,
        endEvening = evening6Below
      ) -> {
      DayPeriod.BLUE_HOUR
    }
    !useGoldenBlueHour &&
      now.isEqualOrAfterOtherNotNull(sunrise) &&
      now.isBeforeOtherNotNull(sunset) -> {
      DayPeriod.DAY
    }
    useGoldenBlueHour &&
      now.isEqualOrAfterOtherNotNull(morning6Above) &&
      now.isBeforeOtherNotNull(evening6Above) -> {
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
        DayMode.SUNSET -> evening18Below?.toLocalTime() ?: noon
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning18Below?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> evening12Below?.toLocalTime() ?: noon
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning12Below?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> evening6Below?.toLocalTime() ?: noon
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Below?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> sunset?.toLocalTime() ?: noon
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunrise?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> noon
      }
    }
    DayPeriod.GOLDEN_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning4Below?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> evening6Above?.toLocalTime() ?: noon
      }
    }
    DayPeriod.BLUE_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Below?.toLocalTime() ?: nightStart
        DayMode.SUNSET -> evening4Below?.toLocalTime() ?: noon
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
        DayMode.SUNRISE -> morning18Below?.toLocalTime() ?: noon
        DayMode.SUNSET -> nightEnd
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning12Below?.toLocalTime() ?: noon
        DayMode.SUNSET -> evening18Below?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Below?.toLocalTime() ?: noon
        DayMode.SUNSET -> evening12Below?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunrise?.toLocalTime() ?: noon
        DayMode.SUNSET -> evening6Below?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> noon
        DayMode.SUNSET -> sunset?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.GOLDEN_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Above?.toLocalTime() ?: noon
        DayMode.SUNSET -> evening4Below?.toLocalTime() ?: nightEnd
      }
    }
    DayPeriod.BLUE_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning4Below?.toLocalTime() ?: noon
        DayMode.SUNSET -> evening6Above?.toLocalTime() ?: nightEnd
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
