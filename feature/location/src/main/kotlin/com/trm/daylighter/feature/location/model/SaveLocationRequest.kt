package com.trm.daylighter.feature.location.model

sealed interface SaveLocationRequest {
  data class User(val zoom: Double) : SaveLocationRequest

  data class Specified(val latitude: Double, val longitude: Double) : SaveLocationRequest

  object CancelCurrent : SaveLocationRequest
}
