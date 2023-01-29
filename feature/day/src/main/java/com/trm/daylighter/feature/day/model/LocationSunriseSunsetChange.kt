package com.trm.daylighter.feature.day.model

import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.model.SunriseSunsetChange

data class LocationSunriseSunsetChange(
  val location: Location,
  val sunriseSunsetChange: SunriseSunsetChange
)
