package com.trm.daylighter.domain.repo

import com.trm.daylighter.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepo {
  suspend fun saveLocation(latitude: Double, longitude: Double)

  fun getAllFlow(): Flow<List<Location>>

  fun getDefaultFlow(): Flow<Location?>
}
