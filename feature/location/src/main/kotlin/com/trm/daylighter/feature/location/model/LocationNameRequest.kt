package com.trm.daylighter.feature.location.model

sealed interface LocationNameRequest {
  data class Geocode(val lat: Double, val lng: Double) : LocationNameRequest

  data class UserInput(val name: String) : LocationNameRequest

  data object Clear : LocationNameRequest
}
