package com.trm.daylighter.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationSunriseSunsetChange(
  val location: Location,
  val today: SunriseSunset,
  val yesterday: SunriseSunset,
)
