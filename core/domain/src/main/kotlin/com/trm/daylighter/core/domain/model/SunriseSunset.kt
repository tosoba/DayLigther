package com.trm.daylighter.core.domain.model

import com.trm.daylighter.core.domain.serializer.LocalDateSerializer
import com.trm.daylighter.core.domain.serializer.LocalDateTimeSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunset(
  @Serializable(with = LocalDateTimeSerializer::class) val morning18Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val evening18Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val morning6Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val evening6Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val morning12Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val evening12Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val sunrise: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val sunset: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val morning6Above: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val morning4Below: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val evening6Above: LocalDateTime?,
  @Serializable(with = LocalDateTimeSerializer::class) val evening4Below: LocalDateTime?,
  @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
)
