package com.trm.daylighter.core.domain.repo

import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange

interface SunriseSunsetRepo {
  suspend fun sync(): Boolean

  suspend fun getLocationSunriseSunsetChangeAtIndex(index: Int): LocationSunriseSunsetChange?

  suspend fun getDefaultLocationSunriseSunsetChange(): LocationSunriseSunsetChange?

  suspend fun deleteForEachLocationExceptMostRecent(limit: Int)
}
