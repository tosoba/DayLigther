package com.trm.daylighter.data.repo

import com.trm.daylighter.data.di.TimeZoneEngineAsyncProvider
import com.trm.daylighter.data.mapper.asDomainModel
import com.trm.daylighter.database.dao.LocationDao
import com.trm.daylighter.database.entity.LocationEntity
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.repo.LocationRepo
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepoImpl
@Inject
constructor(
  private val dao: LocationDao,
  private val timeZoneEngineAsyncProvider: TimeZoneEngineAsyncProvider,
) : LocationRepo {
  override suspend fun saveLocation(latitude: Double, longitude: Double) {
    val zoneId =
      timeZoneEngineAsyncProvider.engine
        .await()
        .query(latitude, longitude)
        .orElse(ZoneId.systemDefault())
    dao.insert(latitude = latitude, longitude = longitude, zoneId = zoneId)
  }

  override fun getAllLocationsFlow(): Flow<List<Location>> =
    dao.selectAllFlow().map { it.map(LocationEntity::asDomainModel) }

  override fun getLocationsCountFlow(): Flow<Int> = dao.selectCountAllFlow()

  override fun getDefaultLocationFlow(): Flow<Location?> =
    dao.selectDefaultFlow().map { it?.asDomainModel() }

  override suspend fun deleteLocationByIdAndGetCountAll(id: Long, isDefault: Boolean): Int =
    dao.deleteByIdAndSelectCountAll(id, isDefault)

  override suspend fun setDefaultLocationById(id: Long) {
    dao.updateDefaultLocationById(id)
  }
}
