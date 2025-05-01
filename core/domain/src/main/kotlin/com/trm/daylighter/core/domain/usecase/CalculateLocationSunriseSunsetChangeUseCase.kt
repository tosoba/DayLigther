package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class CalculateLocationSunriseSunsetChangeUseCase
@Inject
constructor(private val calculateSunriseSunsetUseCase: CalculateSunriseSunsetUseCase) {
  operator fun invoke(location: Location): LocationSunriseSunsetChange {
    val today = LocalDate.now(location.zoneId)
    val timeZone = TimeZone.getTimeZone(location.zoneId.id)
    return LocationSunriseSunsetChange(
      location = location,
      today =
        calculateSunriseSunsetUseCase(
          today.atStartOfDay(),
          location.latitude,
          location.longitude,
          timeZone,
        ),
      yesterday =
        calculateSunriseSunsetUseCase(
          today.minusDays(1L).atStartOfDay(),
          location.latitude,
          location.longitude,
          timeZone,
        ),
    )
  }
}
