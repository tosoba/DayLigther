package com.trm.daylighter.core.network.model

import com.trm.daylighter.core.network.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResult(
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("astronomical_twilight_begin")
  val astronomicalTwilightBegin: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("astronomical_twilight_end")
  val astronomicalTwilightEnd: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("civil_twilight_begin")
  val civilTwilightBegin: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("civil_twilight_end")
  val civilTwilightEnd: LocalDateTime,
  @SerialName("day_length") val dayLengthSeconds: Int,
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("nautical_twilight_begin")
  val nauticalTwilightBegin: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("nautical_twilight_end")
  val nauticalTwilightEnd: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class)
  @SerialName("solar_noon")
  val solarNoon: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class) @SerialName("sunrise") val sunrise: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class) @SerialName("sunset") val sunset: LocalDateTime,
)
