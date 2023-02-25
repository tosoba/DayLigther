package com.trm.daylighter.core.data.repo

import androidx.room.withTransaction
import com.trm.daylighter.core.common.di.DaylighterDispatchers
import com.trm.daylighter.core.common.di.Dispatcher
import com.trm.daylighter.core.data.di.TimeZoneEngineAsyncProvider
import com.trm.daylighter.core.data.mapper.asDomainModel
import com.trm.daylighter.core.database.DaylighterDatabase
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.dao.SunriseSunsetDao
import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.repo.LocationRepo
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocationRepoImpl
@Inject
constructor(
  private val db: DaylighterDatabase,
  private val locationDao: LocationDao,
  private val sunriseSunsetDao: SunriseSunsetDao,
  private val timeZoneEngineAsyncProvider: TimeZoneEngineAsyncProvider,
  @Dispatcher(DaylighterDispatchers.DEFAULT) private val defaultDispatcher: CoroutineDispatcher,
) : LocationRepo {
  override suspend fun saveLocation(latitude: Double, longitude: Double) {
    val zoneId = withContext(defaultDispatcher) { getTimeZoneId(latitude, longitude) }
    locationDao.insert(latitude = latitude, longitude = longitude, zoneId = zoneId)
  }

  override fun getAllLocationsFlow(): Flow<List<Location>> =
    locationDao.selectAllFlow().map { it.map(LocationEntity::asDomainModel) }

  override fun getLocationsCountFlow(): Flow<Int> = locationDao.selectCountAllFlow()

  override fun getDefaultLocationFlow(): Flow<Location?> =
    locationDao.selectDefaultFlow().map { it?.asDomainModel() }

  override suspend fun deleteLocationByIdAndGetCountAll(id: Long, isDefault: Boolean): Int =
    locationDao.deleteByIdAndSelectCountAll(id, isDefault)

  override suspend fun setDefaultLocationById(id: Long) {
    locationDao.updateDefaultLocationById(id)
  }

  override suspend fun getLocationById(id: Long): Location =
    locationDao.selectById(id).asDomainModel()

  override suspend fun updateLocationLatLngById(id: Long, latitude: Double, longitude: Double) {
    val zoneId = getTimeZoneId(latitude, longitude)
    db.withTransaction {
      locationDao.updateLocationLatLngById(
        id = id,
        latitude = latitude,
        longitude = longitude,
        zoneId = zoneId
      )
      sunriseSunsetDao.deleteByLocationId(locationId = id)
    }
  }

  private suspend fun getTimeZoneId(latitude: Double, longitude: Double): ZoneId =
    timeZoneEngineAsyncProvider.engine
      .await()
      .query(latitude, longitude)
      .orElse(ZoneId.systemDefault())
}
