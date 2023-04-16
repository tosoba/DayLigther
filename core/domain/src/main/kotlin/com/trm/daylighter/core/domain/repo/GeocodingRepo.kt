package com.trm.daylighter.core.domain.repo

interface GeocodingRepo {
  suspend fun getLocationDisplayName(lat: Double, lng: Double): String?
}
