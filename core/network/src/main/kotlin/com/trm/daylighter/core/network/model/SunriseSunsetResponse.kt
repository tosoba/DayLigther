package com.trm.daylighter.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResponse(
  @SerialName("results") val content: SunriseSunsetResult?,
  @SerialName("status") val status: String?,
)
