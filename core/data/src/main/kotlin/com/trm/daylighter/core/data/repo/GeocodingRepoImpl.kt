package com.trm.daylighter.core.data.repo

import com.trm.daylighter.core.domain.repo.GeocodingRepo
import com.trm.daylighter.core.network.retrofit.NominatimEndpoint
import javax.inject.Inject

class GeocodingRepoImpl
@Inject
constructor(
  private val endpoint: NominatimEndpoint,
) : GeocodingRepo {
  override suspend fun getLocationDisplayName(lat: Double, lng: Double): String? =
    endpoint.getAddress(lat, lng, "therealmerengue@gmail.com").displayName
}
