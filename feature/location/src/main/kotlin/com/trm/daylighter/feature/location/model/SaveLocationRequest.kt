package com.trm.daylighter.feature.location.model

sealed interface SaveLocationRequest {
  data object User : SaveLocationRequest

  data class Specified(val latitude: Double, val longitude: Double) : SaveLocationRequest

  data object CancelCurrent : SaveLocationRequest
}
