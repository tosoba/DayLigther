package com.trm.daylighter.core.network.retrofit

import com.trm.daylighter.core.network.model.AddressResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimEndpoint {
  @GET("/reverse?format=jsonv2")
  suspend fun getAddress(
    @Query("lat") lat: Double,
    @Query("lon") lon: Double,
    @Query("email") email: String,
    @Query("zoom") zoom: Int? = null
  ): AddressResponse

  companion object {
    internal const val BASE_URL = "https://nominatim.openstreetmap.org/"
  }
}
