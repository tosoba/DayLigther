package com.trm.daylighter.core.testing.model

import com.trm.daylighter.core.domain.model.Location
import java.time.LocalDateTime
import java.time.ZoneId

fun testLocation(
  id: Long = 0,
  latitude: Double = 0.0,
  longitude: Double = 0.0,
  name: String = "",
  isDefault: Boolean = true,
  updatedAt: LocalDateTime = LocalDateTime.now(),
  zoneId: ZoneId = ZoneId.systemDefault()
): Location =
  Location(
    id = id,
    latitude = latitude,
    longitude = longitude,
    name = name,
    isDefault = isDefault,
    updatedAt = updatedAt,
    zoneId = zoneId
  )
