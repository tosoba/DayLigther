package com.trm.daylighter.core.data.mapper

import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.domain.model.Location

fun LocationEntity.asDomainModel(): Location =
  Location(
    id = id,
    latitude = latitude,
    longitude = longitude,
    name = name,
    isDefault = isDefault,
    updatedAt = updatedAt,
    zoneId = zoneId,
  )
