package com.trm.daylighter.core.domain.model

import com.trm.daylighter.core.domain.serializer.LocalDateTimeSerializer
import com.trm.daylighter.core.domain.serializer.ZoneIdSerializer
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.serialization.Serializable

@Serializable
data class Location(
  val id: Long = 0,
  val latitude: Double,
  val longitude: Double,
  val name: String,
  val isDefault: Boolean,
  @Serializable(with = LocalDateTimeSerializer::class) val updatedAt: LocalDateTime,
  @Serializable(with = ZoneIdSerializer::class) val zoneId: ZoneId,
)
