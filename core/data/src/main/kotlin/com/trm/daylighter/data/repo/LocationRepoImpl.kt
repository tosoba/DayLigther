package com.trm.daylighter.data.repo

import com.trm.daylighter.data.mapper.asDomainModel
import com.trm.daylighter.database.dao.LocationDao
import com.trm.daylighter.database.entity.LocationEntity
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepoImpl @Inject constructor(private val dao: LocationDao) : LocationRepo {
  override suspend fun saveLocation(latitude: Double, longitude: Double) {
    dao.insert(latitude = latitude, longitude = longitude)
  }

  override fun getAllFlow(): Flow<List<Location>> =
    dao.selectAllFlow().map { it.map(LocationEntity::asDomainModel) }

  override fun getDefaultFlow(): Flow<Location?> =
    dao.selectDefaultFlow().map { it?.asDomainModel() }
}
