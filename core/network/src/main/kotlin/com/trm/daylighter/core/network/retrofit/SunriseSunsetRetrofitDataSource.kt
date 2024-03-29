package com.trm.daylighter.core.network.retrofit

import com.trm.daylighter.core.network.SunriseSunsetNetworkDataSource
import com.trm.daylighter.core.network.model.SunriseSunsetResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SunriseSunsetRetrofitDataSource
@Inject
constructor(
  private val api: SunriseSunsetEndpoint,
) : SunriseSunsetNetworkDataSource {
  override suspend fun getSunriseSunset(
    lat: Double,
    lng: Double,
    date: LocalDate
  ): SunriseSunsetResult? =
    api.getSunriseSunset(lat, lng, DateTimeFormatter.ISO_DATE.format(date)).content
}
