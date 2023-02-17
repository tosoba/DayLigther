package com.trm.daylighter.core.data.repo

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.trm.daylighter.core.common.util.suspendRunCatching
import com.trm.daylighter.core.data.mapper.asDomainModel
import com.trm.daylighter.core.data.mapper.asEntity
import com.trm.daylighter.core.data.util.timezoneAdjusted
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.dao.SunriseSunsetDao
import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.database.entity.SunriseSunsetEntity
import com.trm.daylighter.core.domain.exception.EmptyAPIResultException
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SunriseSunsetRepoImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
  private val locationDao: LocationDao,
  private val sunriseSunsetDao: SunriseSunsetDao,
  private val network: DaylighterNetworkDataSource,
  private val syncWorkRequest: PeriodicWorkRequest,
) : SunriseSunsetRepo {
  override fun enqueueSync() {
    WorkManager.getInstance(context)
      .enqueueUniquePeriodicWork(SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, syncWorkRequest)
  }

  override fun cancelSync() {
    WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
  }

  override suspend fun sync(): Boolean =
    suspendRunCatching {
        val locations = locationDao.selectAll()
        val mostRecentExistingSunriseSunsets =
          sunriseSunsetDao
            .selectMostRecentForEachLocationId(
              locationIds = locations.map(LocationEntity::id),
              limit = 2,
            )
            .groupBy(SunriseSunsetEntity::locationId)
            .mapValues { (_, sunriseSunsets) ->
              sunriseSunsets.associateBy(SunriseSunsetEntity::date)
            }

        locations.forEach { location ->
          val locationSunriseSunsets = mostRecentExistingSunriseSunsets[location.id] ?: emptyMap()
          val today = LocalDate.now(location.zoneId)
          val dates = listOf(today.minusDays(1L), today)
          val downloaded =
            dates.filterNot(locationSunriseSunsets::containsKey).associateWith { date ->
              network.getSunriseSunset(
                lat = location.latitude,
                lng = location.longitude,
                date = date
              )
            }
          if (downloaded.isEmpty()) return@forEach

          if (downloaded.any { (_, result) -> result == null }) {
            Log.e("Sync", "One of the results from API was null.")
            return@suspendRunCatching false
          }

          sunriseSunsetDao.insertMany(
            downloaded.map { (date, result) ->
              requireNotNull(result)
                .timezoneAdjusted(zoneId = location.zoneId)
                .asEntity(locationId = location.id, date = date)
            }
          )
        }

        true
      }
      .isSuccess

  override suspend fun getLocationSunriseSunsetChangeById(id: Long): LocationSunriseSunsetChange {
    val location = locationDao.selectById(id)
    val today = LocalDate.now(location.zoneId)
    val yesterday = today.minusDays(1L)
    val dates = listOf(yesterday, today)

    val existingSunriseSunsets =
      sunriseSunsetDao
        .selectMostRecentByLocationId(locationId = location.id, limit = 2)
        .associateBy(SunriseSunsetEntity::date)
    if (existingSunriseSunsets.keys.containsAll(dates)) {
      return LocationSunriseSunsetChange(
        location = location.asDomainModel(),
        today = requireNotNull(existingSunriseSunsets[today]).asDomainModel(),
        yesterday = requireNotNull(existingSunriseSunsets[yesterday]).asDomainModel()
      )
    }

    val results =
      coroutineScope {
          dates
            .filter { !existingSunriseSunsets.keys.contains(it) }
            .map { date ->
              async {
                date to
                  network.getSunriseSunset(
                    lat = location.latitude,
                    lng = location.longitude,
                    date = date
                  )
              }
            }
        }
        .awaitAll()
        .toMap()
    if (results.any { (_, result) -> result == null }) {
      Log.e("Sync", "One of the results from API was null.")
      throw EmptyAPIResultException
    }

    val downloadedSunriseSunsets =
      results.mapValues { (date, result) ->
        requireNotNull(result)
          .timezoneAdjusted(zoneId = location.zoneId)
          .asEntity(locationId = id, date = date)
      }
    sunriseSunsetDao.insertMany(downloadedSunriseSunsets.values)

    return LocationSunriseSunsetChange(
      location = location.asDomainModel(),
      today =
        requireNotNull(existingSunriseSunsets[today] ?: downloadedSunriseSunsets[today])
          .asDomainModel(),
      yesterday =
        requireNotNull(existingSunriseSunsets[yesterday] ?: downloadedSunriseSunsets[yesterday])
          .asDomainModel()
    )
  }

  override suspend fun deleteForEachLocationExceptMostRecent(limit: Int) {
    sunriseSunsetDao.deleteForEachLocationExceptMostRecent(limit)
  }

  companion object {
    private const val SYNC_WORK_NAME = "SyncWork"
  }
}
