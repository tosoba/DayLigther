package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.SunriseSunsetChange
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class CalculateSunriseSunsetChangeUseCase
@Inject
constructor(private val calculateSunriseSunsetUseCase: CalculateSunriseSunsetUseCase) {
  operator fun invoke(location: Location): SunriseSunsetChange {
    val today = LocalDate.now(location.zoneId)
    val timeZone = TimeZone.getTimeZone(location.zoneId.id)
    return SunriseSunsetChange(
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
