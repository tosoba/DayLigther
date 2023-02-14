package com.trm.daylighter.core.data.repo

import com.trm.daylighter.core.data.di.TimeZoneEngineAsyncProvider
import com.trm.daylighter.core.data.mapper.asDomainModel
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.repo.LocationRepo
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

  override suspend fun getLocationById(id: Long): Location = dao.selectById(id).asDomainModel()

  override suspend fun updateLocationLatLngById(id: Long, latitude: Double, longitude: Double) {
    dao.updateLocationLatLngById(id = id, latitude = latitude, longitude = longitude)
  }
}
