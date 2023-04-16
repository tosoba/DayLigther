package com.trm.daylighter.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressResponse(
  @SerialName("address") val address: Address?,
  @SerialName("addresstype") val addresstype: String?,
  @SerialName("boundingbox") val boundingbox: List<String>?,
  @SerialName("category") val category: String?,
  @SerialName("display_name") val displayName: String?,
  @SerialName("importance") val importance: Double?,
  @SerialName("lat") val lat: String?,
  @SerialName("licence") val licence: String?,
  @SerialName("lon") val lon: String?,
  @SerialName("name") val name: String?,
  @SerialName("osm_id") val osmId: Int?,
  @SerialName("osm_type") val osmType: String?,
  @SerialName("place_id") val placeId: Int?,
  @SerialName("place_rank") val placeRank: Int?,
  @SerialName("type") val type: String?
)
