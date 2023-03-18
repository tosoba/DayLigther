package com.trm.daylighter.feature.location.model

sealed interface SaveLocationType {
  object User : SaveLocationType
  data class Specified(val latitude: Double, val longitude: Double) : SaveLocationType
  object CanceCurrent : SaveLocationType
}
