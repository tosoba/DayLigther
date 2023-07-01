package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.HalfDay
import com.trm.daylighter.core.domain.model.SunPosition
import com.trm.daylighter.core.domain.model.SunriseSunset
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class CalculateSunriseSunsetUseCase
@Inject
constructor(
  private val calculateSunPositionTimestampUseCase: CalculateSunPositionTimestampUseCase
) {
  operator fun invoke(
    date: LocalDateTime,
    latitude: Double,
    longitude: Double,
    timeZone: TimeZone
  ): SunriseSunset {
    fun calculateDawnOrTwilight(sunPosition: SunPosition, halfDay: HalfDay): LocalDateTime? =
      calculateSunPositionTimestampUseCase(
        latitude = latitude,
        longitude = longitude,
        date = date,
        sunPosition = sunPosition,
        halfDay = halfDay,
        timeZone = timeZone
      )

    return SunriseSunset(
      astronomicalTwilightBegin =
        calculateDawnOrTwilight(SunPosition.ASTRONOMICAL, HalfDay.MORNING),
      astronomicalTwilightEnd = calculateDawnOrTwilight(SunPosition.ASTRONOMICAL, HalfDay.EVENING),
      civilTwilightBegin = calculateDawnOrTwilight(SunPosition.CIVIL, HalfDay.MORNING),
      civilTwilightEnd = calculateDawnOrTwilight(SunPosition.CIVIL, HalfDay.EVENING),
      nauticalTwilightBegin = calculateDawnOrTwilight(SunPosition.NAUTICAL, HalfDay.MORNING),
      nauticalTwilightEnd = calculateDawnOrTwilight(SunPosition.NAUTICAL, HalfDay.EVENING),
      sunrise = calculateDawnOrTwilight(SunPosition.OFFICIAL, HalfDay.MORNING),
      sunset = calculateDawnOrTwilight(SunPosition.OFFICIAL, HalfDay.EVENING),
      goldenHourAboveMorning =
        calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_ABOVE, HalfDay.MORNING),
      goldenHourBelowMorning =
        calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_BELOW, HalfDay.MORNING),
      goldenHourAboveEvening =
        calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_ABOVE, HalfDay.EVENING),
      goldenHourBelowEvening =
        calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_BELOW, HalfDay.EVENING),
      blueHourBegin = calculateDawnOrTwilight(SunPosition.BLUE_HOUR, HalfDay.MORNING),
      blueHourEnd = calculateDawnOrTwilight(SunPosition.BLUE_HOUR, HalfDay.EVENING),
      date = date.toLocalDate()
    )
  }
}
