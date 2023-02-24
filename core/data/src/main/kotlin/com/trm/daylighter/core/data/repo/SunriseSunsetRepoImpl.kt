package com.trm.daylighter.core.data.repo

import android.util.Log
import com.trm.daylighter.core.common.util.suspendRunCatching
import com.trm.daylighter.core.data.mapper.asDomainModel
import com.trm.daylighter.core.data.mapper.asEntity
import com.trm.daylighter.core.data.util.timezoneAdjusted
import com.trm.daylighter.core.database.dao.SunriseSunsetDao
import com.trm.daylighter.core.database.entity.SunriseSunsetEntity
import com.trm.daylighter.core.domain.exception.EmptyAPIResultException
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SunriseSunsetRepoImpl
@Inject
constructor(
  private val sunriseSunsetDao: SunriseSunsetDao,
  private val network: DaylighterNetworkDataSource,
) : SunriseSunsetRepo {
  override suspend fun sync(): Boolean =
    suspendRunCatching {
        val existingLocationsSunriseSunsetsList =
          sunriseSunsetDao.selectMostRecentForEachLocation(limit = 2)
        existingLocationsSunriseSunsetsList.forEach { (location, sunriseSunsets) ->
          val today = LocalDate.now(location.zoneId)
          val dates = listOf(today.minusDays(1L), today)

          val sunriseSunsetsDates = sunriseSunsets.map(SunriseSunsetEntity::date).toSet()
          val downloaded =
            dates.filterNot(sunriseSunsetsDates::contains).associateWith { date ->
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
    val existingLocationSunriseSunsets =
      sunriseSunsetDao.selectMostRecentByLocationId(locationId = id, limit = 2).entries.first()
    val existingSunriseSunsets =
      existingLocationSunriseSunsets.value.associateBy(SunriseSunsetEntity::date)
    val location = existingLocationSunriseSunsets.key

    val today = LocalDate.now(location.zoneId)
    val yesterday = today.minusDays(1L)
    val dates = listOf(yesterday, today)

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
}
