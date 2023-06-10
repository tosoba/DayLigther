package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.DawnOrTwilight
import com.trm.daylighter.core.domain.model.HalfDay
import com.trm.daylighter.core.domain.model.SunriseSunset
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class CalculateSunriseSunsetUseCase
@Inject
constructor(private val calculateDawnOrTwilightUseCase: CalculateDawnOrTwilightUseCase) {
  operator fun invoke(
    date: LocalDateTime,
    latitude: Double,
    longitude: Double,
    timeZone: TimeZone
  ): SunriseSunset {
    fun calculateDawnOrTwilight(dawnOrTwilight: DawnOrTwilight, halfDay: HalfDay): LocalDateTime? =
      calculateDawnOrTwilightUseCase(
        latitude = latitude,
        longitude = longitude,
        date = date,
        dawnOrTwilight = dawnOrTwilight,
        halfDay = halfDay,
        timeZone = timeZone
      )

    val sunrise = calculateDawnOrTwilight(DawnOrTwilight.OFFICIAL, HalfDay.MORNING)
    val sunset = calculateDawnOrTwilight(DawnOrTwilight.OFFICIAL, HalfDay.EVENING)

    return SunriseSunset(
      astronomicalTwilightBegin =
        calculateDawnOrTwilight(DawnOrTwilight.ASTRONOMICAL, HalfDay.MORNING),
      astronomicalTwilightEnd =
        calculateDawnOrTwilight(DawnOrTwilight.ASTRONOMICAL, HalfDay.EVENING),
      civilTwilightBegin = calculateDawnOrTwilight(DawnOrTwilight.CIVIL, HalfDay.MORNING),
      civilTwilightEnd = calculateDawnOrTwilight(DawnOrTwilight.CIVIL, HalfDay.EVENING),
      nauticalTwilightBegin = calculateDawnOrTwilight(DawnOrTwilight.NAUTICAL, HalfDay.MORNING),
      nauticalTwilightEnd = calculateDawnOrTwilight(DawnOrTwilight.NAUTICAL, HalfDay.EVENING),
      sunrise = sunrise,
      sunset = sunset,
      date = date.toLocalDate()
    )
  }
}
