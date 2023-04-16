package com.trm.daylighter.core.network

import com.trm.daylighter.core.network.model.SunriseSunsetResult
import java.time.LocalDate

interface SunriseSunsetNetworkDataSource {
  suspend fun getSunriseSunset(lat: Double, lng: Double, date: LocalDate): SunriseSunsetResult?
}
