package com.trm.daylighter.core.domain.repo

import com.trm.daylighter.core.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepo {
  suspend fun saveLocation(latitude: Double, longitude: Double)

  fun getAllLocationsFlow(): Flow<List<Location>>

  fun getLocationsCountFlow(): Flow<Int>

  fun getDefaultLocationFlow(): Flow<Location?>

  suspend fun deleteLocationByIdAndGetCountAll(id: Long, isDefault: Boolean): Int

  suspend fun setDefaultLocationById(id: Long)

  suspend fun getLocationById(id: Long): Location
}
