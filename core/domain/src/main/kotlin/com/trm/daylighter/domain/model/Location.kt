package com.trm.daylighter.domain.model

import java.time.ZonedDateTime

data class Location(
  val id: Long = 0,
  val latitude: Double,
  val longitude: Double,
  val isDefault: Boolean,
  val updatedAt: ZonedDateTime,
)
