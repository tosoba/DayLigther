package com.trm.daylighter.core.domain.model

data class LocationSunriseSunsetChange(
  val location: Location,
  val today: SunriseSunset,
  val yesterday: SunriseSunset,
)
