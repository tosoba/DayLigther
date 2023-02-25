package com.trm.daylighter.core.domain.repo

import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import kotlinx.coroutines.flow.Flow

interface SunriseSunsetRepo {
  suspend fun sync(): Boolean

  suspend fun getLocationSunriseSunsetChangeAtIndex(index: Int): LocationSunriseSunsetChange?

  suspend fun getDefaultLocationSunriseSunsetChange(): Flow<Loadable<LocationSunriseSunsetChange>>

  suspend fun deleteForEachLocationExceptMostRecent(limit: Int)
}
