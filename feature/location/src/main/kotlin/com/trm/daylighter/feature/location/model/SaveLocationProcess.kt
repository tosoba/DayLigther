package com.trm.daylighter.feature.location.model

import androidx.compose.runtime.Stable

@Stable
data class SaveLocationProcess(val latitude: Double, val longitude: Double, val isUser: Boolean)
