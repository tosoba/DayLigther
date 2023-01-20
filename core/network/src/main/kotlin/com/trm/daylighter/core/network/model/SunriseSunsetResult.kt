package com.trm.daylighter.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResult(
  @SerialName("astronomical_twilight_begin") val astronomicalTwilightBegin: String?,
  @SerialName("astronomical_twilight_end") val astronomicalTwilightEnd: String?,
  @SerialName("civil_twilight_begin") val civilTwilightBegin: String?,
  @SerialName("civil_twilight_end") val civilTwilightEnd: String?,
  @SerialName("day_length") val dayLength: Int?,
  @SerialName("nautical_twilight_begin") val nauticalTwilightBegin: String?,
  @SerialName("nautical_twilight_end") val nauticalTwilightEnd: String?,
  @SerialName("solar_noon") val solarNoon: String?,
  @SerialName("sunrise") val sunrise: String?,
  @SerialName("sunset") val sunset: String?
)
