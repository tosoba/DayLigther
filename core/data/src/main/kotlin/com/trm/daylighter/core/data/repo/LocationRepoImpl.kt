package com.trm.daylighter.core.data.repo

import com.trm.daylighter.core.data.mapper.asDomainModel
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.domain.di.DaylighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.repo.LocationRepo
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import us.dustinj.timezonemap.TimeZoneMap

class LocationRepoImpl
@Inject
constructor(
  private val locationDao: LocationDao,
  @Dispatcher(DaylighterDispatchers.DEFAULT) private val defaultDispatcher: CoroutineDispatcher,
) : LocationRepo {
  override suspend fun saveLocation(latitude: Double, longitude: Double, name: String) {
    locationDao.insert(
      latitude = latitude,
      longitude = longitude,
      name = name,
      zoneId = getTimeZoneId(latitude = latitude, longitude = longitude)
    )
  }

  override fun getAllLocationsFlow(): Flow<List<Location>> =
    locationDao.selectAllFlow().map { it.map(LocationEntity::asDomainModel) }

  override fun getDefaultLocationFlow(): Flow<Location?> =
    locationDao.selectDefaultFlow().map { it?.asDomainModel() }

  override suspend fun getDefaultLocation(): Location? =
    locationDao.selectDefault()?.asDomainModel()

  override suspend fun deleteAllLocations() {
    locationDao.deleteAll()
  }

  override suspend fun deleteLocationById(id: Long, isDefault: Boolean) {
    locationDao.deleteById(id, isDefault)
  }

  override suspend fun setDefaultLocationById(id: Long) {
    locationDao.updateDefaultLocationById(id)
  }

  override suspend fun getLocationById(id: Long): Location? =
    locationDao.selectById(id)?.asDomainModel()

  override suspend fun updateLocationLatLngById(
    id: Long,
    latitude: Double,
    longitude: Double,
    name: String
  ) {
    locationDao.updateLocationLatLngById(
      id = id,
      latitude = latitude,
      longitude = longitude,
      name = name,
      zoneId = getTimeZoneId(latitude = latitude, longitude = longitude)
    )
  }

  private suspend fun getTimeZoneId(latitude: Double, longitude: Double): ZoneId =
    withContext(defaultDispatcher) {
      TimeZoneMap.forRegion(
          minDegreesLatitude = latitude - 1.0,
          minDegreesLongitude = longitude - 1.0,
          maxDegreesLatitude = latitude + 1.0,
          maxDegreesLongitude = longitude + 1.0
        )
        .getOverlappingTimeZone(degreesLatitude = latitude, degreesLongitude = longitude)
        ?.zoneId
        ?.let(ZoneId::of)
        ?: ZoneId.systemDefault()
    }

  override suspend fun getNonDefaultLocationOffsetById(id: Long): Int? =
    locationDao.selectNonDefaultLocationIndexById(id)
}
