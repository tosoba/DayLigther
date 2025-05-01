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
    timeZone: TimeZone,
  ): SunriseSunset {
    fun calculateDawnOrTwilight(sunPosition: SunPosition, halfDay: HalfDay): LocalDateTime? =
      calculateSunPositionTimestampUseCase(
        latitude = latitude,
        longitude = longitude,
        date = date,
        sunPosition = sunPosition,
        halfDay = halfDay,
        timeZone = timeZone,
      )

    return SunriseSunset(
      morning18Below = calculateDawnOrTwilight(SunPosition.ASTRONOMICAL, HalfDay.MORNING),
      evening18Below = calculateDawnOrTwilight(SunPosition.ASTRONOMICAL, HalfDay.EVENING),
      morning6Below = calculateDawnOrTwilight(SunPosition.CIVIL, HalfDay.MORNING),
      evening6Below = calculateDawnOrTwilight(SunPosition.CIVIL, HalfDay.EVENING),
      morning12Below = calculateDawnOrTwilight(SunPosition.NAUTICAL, HalfDay.MORNING),
      evening12Below = calculateDawnOrTwilight(SunPosition.NAUTICAL, HalfDay.EVENING),
      sunrise = calculateDawnOrTwilight(SunPosition.OFFICIAL, HalfDay.MORNING),
      sunset = calculateDawnOrTwilight(SunPosition.OFFICIAL, HalfDay.EVENING),
      morning6Above = calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_ABOVE, HalfDay.MORNING),
      morning4Below = calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_BELOW, HalfDay.MORNING),
      evening6Above = calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_ABOVE, HalfDay.EVENING),
      evening4Below = calculateDawnOrTwilight(SunPosition.GOLDEN_HOUR_BELOW, HalfDay.EVENING),
      date = date.toLocalDate(),
    )
  }
}
