package com.trm.daylighter.core.data.repo

import com.trm.daylighter.core.domain.repo.GeocodingRepo
import fr.dudie.nominatim.client.JsonNominatimClient
import javax.inject.Inject

class GeocodingRepoImpl
@Inject
constructor(
  private val nominatim: JsonNominatimClient,
) : GeocodingRepo {
  override fun getLocationDisplayName(lat: Double, lng: Double, zoom: Int): String? =
    nominatim.getAddress(lng, lat, zoom)?.displayName
}
