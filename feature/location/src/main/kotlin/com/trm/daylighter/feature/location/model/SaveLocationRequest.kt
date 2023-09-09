package com.trm.daylighter.feature.location.model

sealed interface SaveLocationRequest {
  object User : SaveLocationRequest

  data class Specified(val latitude: Double, val longitude: Double) : SaveLocationRequest

  object CancelCurrent : SaveLocationRequest
}
