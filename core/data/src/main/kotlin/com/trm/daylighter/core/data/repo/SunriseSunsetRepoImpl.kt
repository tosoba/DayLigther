package com.trm.daylighter.core.data.repo

import android.util.Log
import androidx.room.withTransaction
import com.trm.daylighter.core.common.util.suspendRunCatching
import com.trm.daylighter.core.data.mapper.asDomainModel
import com.trm.daylighter.core.data.mapper.asEntity
import com.trm.daylighter.core.data.util.timezoneAdjusted
import com.trm.daylighter.core.database.DaylighterDatabase
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.dao.SunriseSunsetDao
import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.database.entity.SunriseSunsetEntity
import com.trm.daylighter.core.domain.exception.EmptyAPIResultException
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

class SunriseSunsetRepoImpl
@Inject
constructor(
  private val db: DaylighterDatabase,
  private val locationDao: LocationDao,
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

  override suspend fun getLocationSunriseSunsetChangeAtIndex(
    index: Int
  ): LocationSunriseSunsetChange? {
    val (location, existingSunriseSunsets) =
      db.withTransaction {
        val location =
          locationDao.selectLocationAtOffset(offset = index) ?: return@withTransaction null
        val sunriseSunsets =
          sunriseSunsetDao
            .selectMostRecentByLocationId(locationId = location.id, limit = 2)
            .associateBy(SunriseSunsetEntity::date)
        location to sunriseSunsets
      }
        ?: return null

    return mapToSunriseSunsetChange(location, existingSunriseSunsets)
  }

  override suspend fun deleteForEachLocationExceptMostRecent(limit: Int) {
    sunriseSunsetDao.deleteForEachLocationExceptMostRecent(limit)
  }

  override suspend fun getDefaultLocationSunriseSunsetChange():
    Flow<Loadable<LocationSunriseSunsetChange>> =
    sunriseSunsetDao.selectMostRecentForDefaultLocation(limit = 2).transformLatest {
      emit(LoadingFirst)
      val (location, existingSunriseSunsets) =
        it
          .mapValues { (_, sunriseSunsets) ->
            sunriseSunsets.associateBy(SunriseSunsetEntity::date)
          }
          .entries
          .firstOrNull()
          ?: run {
            emit(Empty)
            return@transformLatest
          }
      try {
        emit(mapToSunriseSunsetChange(location, existingSunriseSunsets).asLoadable())
      } catch (ex: CancellationException) {
        throw ex
      } catch (ex: Exception) {
        emit(FailedFirst(ex))
      }
    }

  private suspend fun mapToSunriseSunsetChange(
    location: LocationEntity,
    existingSunriseSunsets: Map<LocalDate, SunriseSunsetEntity>
  ): LocationSunriseSunsetChange {
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

    val downloadedSunriseSunsets =
      getSunriseSunsetsFromNetworkFor(
        dates = dates.filterNot(existingSunriseSunsets::containsKey),
        location = location
      )
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

  private suspend fun getSunriseSunsetsFromNetworkFor(
    dates: List<LocalDate>,
    location: LocationEntity
  ): Map<LocalDate, SunriseSunsetEntity> {
    val results =
      coroutineScope {
          dates.map { date ->
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

    return results.mapValues { (date, result) ->
      requireNotNull(result)
        .timezoneAdjusted(zoneId = location.zoneId)
        .asEntity(locationId = location.id, date = date)
    }
  }
}
