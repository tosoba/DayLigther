package com.trm.daylighter.core.domain.model

import com.trm.daylighter.core.domain.serializer.ZoneIdSerializer
import com.trm.daylighter.core.domain.serializer.ZonedDateTimeSerializer
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Location(
  val id: Long = 0,
  val latitude: Double,
  val longitude: Double,
  val name: String,
  val isDefault: Boolean,
  @Serializable(with = ZonedDateTimeSerializer::class) val updatedAt: ZonedDateTime,
  @Serializable(with = ZoneIdSerializer::class) val zoneId: ZoneId,
)
