package com.trm.daylighter.core.network.retrofit

import com.trm.daylighter.core.network.model.SunriseSunsetResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DaylighterApi {
  @GET("/json?formatted=0")
  suspend fun getSunriseSunset(
    @Query("lat") lat: Double,
    @Query("lng") lng: Double,
    @Query("date") date: String
  ): SunriseSunsetResponse

  companion object {
    internal const val BASE_URL = "https://api.sunrise-sunset.org/"
  }
}
