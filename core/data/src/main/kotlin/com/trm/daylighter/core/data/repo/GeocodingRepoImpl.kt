package com.trm.daylighter.core.data.repo

import com.trm.daylighter.core.domain.repo.GeocodingRepo
import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import com.trm.daylighter.core.network.retrofit.NominatimEndpoint
import javax.inject.Inject
import timber.log.Timber

class GeocodingRepoImpl
@Inject
constructor(
  private val endpoint: NominatimEndpoint,
  private val preferenceDataSource: PreferenceDataSource
) : GeocodingRepo {
  override suspend fun getLocationDisplayName(lat: Double, lng: Double): String? {
    val email =
      preferenceDataSource.getGeocodingEmail()
        ?: run {
          Timber.e("Geocoding email preference is not set.")
          return null
        }
    return endpoint.getAddress(lat = lat, lon = lng, email = email).displayName
  }
}
