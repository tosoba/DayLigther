package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.GeocodingRepo
import javax.inject.Inject

class GetLocationDisplayName @Inject constructor(private val geocodingRepo: GeocodingRepo) {
  operator fun invoke(lat: Double, lng: Double, zoom: Int = 5): String? =
    geocodingRepo.getLocationDisplayName(lat = lat, lng = lng, zoom = zoom)
}
