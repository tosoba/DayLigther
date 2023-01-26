package com.trm.daylighter.data.repo

import android.util.Log
import com.trm.daylighter.core.common.util.suspendRunCatching
import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import com.trm.daylighter.data.mapper.asEntity
import com.trm.daylighter.database.dao.SunriseSunsetDao
import com.trm.daylighter.database.entity.SunriseSunsetEntity
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import java.time.LocalDate
import javax.inject.Inject

class SunriseSunsetRepoImpl
@Inject
constructor(
  private val dao: SunriseSunsetDao,
  private val network: DaylighterNetworkDataSource,
) : SunriseSunsetRepo {
  override suspend fun sync(): Boolean =
    suspendRunCatching {
        val today = LocalDate.now()
        val dates = listOf(today.minusDays(1L), today)

        val sunriseSunsetMap = dao.selectByDates(dates).associateBy(SunriseSunsetEntity::date)
        val downloaded =
          dates.filterNot(sunriseSunsetMap::containsKey).associateWith { date ->
            network.getSunriseSunset(lat = WARSAW_LAT, lng = WARSAW_LNG, date = date)
          }
        if (downloaded.isEmpty()) return@suspendRunCatching true
        if (downloaded.any { it.value == null }) {
          Log.e("Sync", "One of the results from API was null.")
          return@suspendRunCatching false
        }

        dao.insertMany(
          downloaded.map { (date, result) -> requireNotNull(result).asEntity(date = date) }
        )

        true
      }
      .isSuccess

  companion object {
    private const val WARSAW_LAT = 52.237049
    private const val WARSAW_LNG = 21.017532
  }
}
