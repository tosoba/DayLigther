package com.trm.daylighter.data.repo

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.trm.daylighter.core.common.util.suspendRunCatching
import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import com.trm.daylighter.data.mapper.asEntity
import com.trm.daylighter.database.dao.LocationDao
import com.trm.daylighter.database.dao.SunriseSunsetDao
import com.trm.daylighter.database.entity.LocationEntity
import com.trm.daylighter.database.entity.SunriseSunsetEntity
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject

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
        val today = LocalDate.now()
        val dates = listOf(today.minusDays(1L), today)

        val locations = locationDao.selectAll()
        val existingSunriseSunsets =
          sunriseSunsetDao
            .selectByLocationIdsAndDates(
              locationIds = locations.map(LocationEntity::id),
              dates = dates
            )
            .groupBy(SunriseSunsetEntity::locationId)
            .mapValues { (_, sunriseSunsets) ->
              sunriseSunsets.associateBy(SunriseSunsetEntity::date)
            }
        locations.forEach { location ->
          val locationSunriseSunsets = existingSunriseSunsets[location.id] ?: emptyMap()
          val downloaded =
            dates.filterNot(locationSunriseSunsets::containsKey).associateWith { date ->
              network.getSunriseSunset(
                lat = location.latitude,
                lng = location.longitude,
                date = date
              )
            }
          if (downloaded.isEmpty()) return@suspendRunCatching true
          if (downloaded.any { it.value == null }) {
            Log.e("Sync", "One of the results from API was null.")
            return@suspendRunCatching false
          }

          sunriseSunsetDao.insertMany(
            downloaded.map { (date, result) ->
              requireNotNull(result).asEntity(locationId = location.id, date = date)
            }
          )
        }

        true
      }
      .isSuccess

  companion object {
    private const val SYNC_WORK_NAME = "SyncWork"
  }
}
