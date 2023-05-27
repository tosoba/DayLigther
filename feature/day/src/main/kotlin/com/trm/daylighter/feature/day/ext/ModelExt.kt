package com.trm.daylighter.feature.day.ext

import androidx.compose.ui.graphics.Color
import com.trm.daylighter.core.common.util.ext.isBeforeOtherNotNull
import com.trm.daylighter.core.common.util.ext.isEqualOrAfterOtherNotNull
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.feature.day.model.DayPeriod
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs

internal fun SunriseSunset.getUpcomingTimestampsSorted(now: LocalDateTime): List<LocalDateTime> =
  allTimestamps().filterNotNull().filter { it.isAfter(now) }.sorted()

internal fun SunriseSunset.allTimestamps(): List<LocalDateTime?> =
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

private fun LocalDateTime.isInPeriod(
  beginMorning: LocalDateTime?,
  endMorning: LocalDateTime?,
  beginEvening: LocalDateTime?,
  endEvening: LocalDateTime?
): Boolean =
  (isEqualOrAfterOtherNotNull(beginMorning) && isBeforeOtherNotNull(endMorning)) ||
    (beginMorning == null && isBeforeOtherNotNull(endMorning)) ||
    (isEqualOrAfterOtherNotNull(beginEvening) && isBeforeOtherNotNull(endEvening)) ||
    (beginEvening == null && isBeforeOtherNotNull(endEvening))

internal fun SunriseSunset.currentPeriodIn(location: Location): DayPeriod {
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
      val decemberSolstice = LocalDateTime.of(now.year, 12, 22, 0, 0)
      val juneSolstice = LocalDateTime.of(now.year, 6, 22, 0, 0)
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
