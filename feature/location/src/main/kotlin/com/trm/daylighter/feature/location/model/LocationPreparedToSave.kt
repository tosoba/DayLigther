package com.trm.daylighter.feature.location.model

import androidx.compose.runtime.Stable

@Stable
data class LocationPreparedToSave(
  val latitude: Double,
  val longitude: Double,
  val zoom: Double,
  val isUser: Boolean
)
