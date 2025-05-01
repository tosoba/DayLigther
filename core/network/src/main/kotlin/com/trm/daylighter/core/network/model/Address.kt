package com.trm.daylighter.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
  @SerialName("building") val building: String?,
  @SerialName("city") val city: String?,
  @SerialName("country") val country: String?,
  @SerialName("country_code") val countryCode: String?,
  @SerialName("neighbourhood") val neighbourhood: String?,
  @SerialName("postcode") val postcode: String?,
  @SerialName("quarter") val quarter: String?,
  @SerialName("road") val road: String?,
  @SerialName("state") val state: String?,
  @SerialName("suburb") val suburb: String?,
)
