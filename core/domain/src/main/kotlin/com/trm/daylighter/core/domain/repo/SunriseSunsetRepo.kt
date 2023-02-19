package com.trm.daylighter.core.domain.repo

import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange

interface SunriseSunsetRepo {
  suspend fun sync(): Boolean

  suspend fun getLocationSunriseSunsetChangeById(id: Long): LocationSunriseSunsetChange

  suspend fun deleteForEachLocationExceptMostRecent(limit: Int)
}
