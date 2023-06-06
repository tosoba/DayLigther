package com.trm.daylighter.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetChange(val today: SunriseSunset, val yesterday: SunriseSunset)
