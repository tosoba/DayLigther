package com.trm.daylighter.domain.repo

import com.trm.daylighter.domain.model.LocationSunriseSunsetChange

interface SunriseSunsetRepo {
  suspend fun sync(): Boolean

  fun enqueueSync()

  fun cancelSync()

  suspend fun getLocationSunriseSunsetChangeById(id: Long): LocationSunriseSunsetChange
}
