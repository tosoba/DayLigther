package com.trm.daylighter.core.domain.repo

interface GeocodingRepo {
  fun getLocationDisplayName(lat: Double, lng: Double, zoom: Int): String?
}
