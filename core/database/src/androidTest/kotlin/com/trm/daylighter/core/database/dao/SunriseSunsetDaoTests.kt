package com.trm.daylighter.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.trm.daylighter.core.database.DaylighterDatabase
import com.trm.daylighter.core.database.entity.SunriseSunsetEntity
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SunriseSunsetDaoTests {
  private lateinit var db: DaylighterDatabase
  private lateinit var locationDao: LocationDao
  private lateinit var sunriseSunsetDao: SunriseSunsetDao

  @Before
  fun createDatabase() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, DaylighterDatabase::class.java).build()
    locationDao = db.locationDao()
    sunriseSunsetDao = db.sunriseSunsetDao()
  }

  @Test
  fun deleteForEachLocationExceptMostRecent() = runTest {
    val limit = 2
    repeat(2) { locationDao.insert(0.0, 0.0, ZoneId.systemDefault()) }
    val insertedLocations = locationDao.selectAll()
    val insertedSunriseSunsets = ArrayList<SunriseSunsetEntity>(15)
    var dateIt1 = LocalDate.now()
    var dateIt2 = dateIt1.minusDays(10L)
    repeat(10) { index ->
      if (index % 2 == 0) {
        getSunriseSunsetWithNowTimestamps(dateIt1, insertedLocations.first().id).also {
          sunriseSunsetDao.insert(it)
          insertedSunriseSunsets.add(it)
        }
        dateIt1 = dateIt1.minusDays(1L)
      }

      getSunriseSunsetWithNowTimestamps(dateIt2, insertedLocations.last().id).also {
        sunriseSunsetDao.insert(it)
        insertedSunriseSunsets.add(it)
      }
      dateIt2 = dateIt2.minusDays(1L)
    }

    sunriseSunsetDao.deleteForEachLocationExceptMostRecent(limit = limit)

    data class RemainingSunriseSunset(val locationId: Long, val date: LocalDate)
    val cursor = db.query("SELECT location_id, date FROM sunrise_sunset", emptyArray())
    val remainingSunriseSunsets = ArrayList<RemainingSunriseSunset>(cursor.count)
    while (cursor.moveToNext()) {
      remainingSunriseSunsets.add(
        RemainingSunriseSunset(
          locationId =
            cursor.getLong(cursor.getColumnIndex(cursor.columnNames.find { it == "location_id" })),
          date =
            LocalDate.parse(
              cursor.getString(cursor.getColumnIndex(cursor.columnNames.find { it == "date" }))
            )
        )
      )
    }
    assertEquals(insertedLocations.size * limit, remainingSunriseSunsets.size)
    remainingSunriseSunsets.groupBy(RemainingSunriseSunset::locationId).values.forEach {
      sunriseSunsetsForLocation ->
      assertEquals(limit, sunriseSunsetsForLocation.size)
      assertTrue {
        insertedSunriseSunsets
          .map(SunriseSunsetEntity::date)
          .sortedDescending()
          .containsAll(sunriseSunsetsForLocation.map(RemainingSunriseSunset::date))
      }
    }
  }

  private fun getSunriseSunsetWithNowTimestamps(
    date: LocalDate,
    locationId: Long,
  ): SunriseSunsetEntity {
    val now = ZonedDateTime.now()
    return SunriseSunsetEntity(
      astronomicalTwilightBegin = now,
      astronomicalTwilightEnd = now,
      civilTwilightBegin = now,
      civilTwilightEnd = now,
      dayLengthSeconds = 0,
      nauticalTwilightBegin = now,
      nauticalTwilightEnd = now,
      solarNoon = now,
      sunrise = now,
      sunset = now,
      date = date,
      locationId = locationId
    )
  }
}
