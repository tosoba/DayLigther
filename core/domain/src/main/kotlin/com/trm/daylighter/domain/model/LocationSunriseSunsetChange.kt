package com.trm.daylighter.domain.model

data class LocationSunriseSunsetChange(
  val location: Location,
  val today: SunriseSunset,
  val yesterday: SunriseSunset,
)
