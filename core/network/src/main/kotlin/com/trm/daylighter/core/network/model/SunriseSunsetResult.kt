package com.trm.daylighter.core.network.model

import com.trm.daylighter.core.network.serializer.ZonedDateTimeSerializer
import java.time.ZonedDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResult(
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("astronomical_twilight_begin")
  val astronomicalTwilightBegin: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("astronomical_twilight_end")
  val astronomicalTwilightEnd: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("civil_twilight_begin")
  val civilTwilightBegin: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("civil_twilight_end")
  val civilTwilightEnd: ZonedDateTime,
  @SerialName("day_length") val dayLength: Int,
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("nautical_twilight_begin")
  val nauticalTwilightBegin: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("nautical_twilight_end")
  val nauticalTwilightEnd: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class)
  @SerialName("solar_noon")
  val solarNoon: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class) @SerialName("sunrise") val sunrise: ZonedDateTime,
  @Serializable(ZonedDateTimeSerializer::class) @SerialName("sunset") val sunset: ZonedDateTime,
)
