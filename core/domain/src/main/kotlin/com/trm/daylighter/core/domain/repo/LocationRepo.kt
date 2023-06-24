package com.trm.daylighter.core.domain.repo

import com.trm.daylighter.core.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepo {
  suspend fun saveLocation(latitude: Double, longitude: Double, name: String)

  fun getAllLocationsFlow(): Flow<List<Location>>

  fun getDefaultLocationFlow(): Flow<Location?>

  suspend fun getDefaultLocation(): Location?

  suspend fun deleteAllLocations()

  suspend fun deleteLocationById(id: Long, isDefault: Boolean)

  suspend fun setDefaultLocationById(id: Long)

  suspend fun getLocationById(id: Long): Location?

  suspend fun updateLocationLatLngById(id: Long, latitude: Double, longitude: Double, name: String)

  suspend fun getNonDefaultLocationOffsetById(id: Long): Int?
}
