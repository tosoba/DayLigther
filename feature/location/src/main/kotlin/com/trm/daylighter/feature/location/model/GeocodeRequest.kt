package com.trm.daylighter.feature.location.model

sealed interface GeocodeRequest {
  data class GetDisplayName(val lat: Double, val lng: Double) : GeocodeRequest
  object CancelCurrent : GeocodeRequest
}
