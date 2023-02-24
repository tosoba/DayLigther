package com.trm.daylighter.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.trm.daylighter.core.database.DaylighterDatabase
import com.trm.daylighter.core.database.entity.LocationEntity
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
    val insertedLocations = insertLocations(amount = 2)
    var dateIt1 = LocalDate.now()
    var dateIt2 = dateIt1.minusDays(10L)
    val insertedSunriseSunsets =
      insertSunriseSunsets(locations = insertedLocations, iterationsForEachLocation = 10) {
        location,
        sunriseSunsetIndex ->
        when (location.id) {
          insertedLocations.first().id -> {
            if (sunriseSunsetIndex % 2 == 0) {
              getSunriseSunsetWithNowTimestamps(dateIt1, location.id).also {
                dateIt1 = dateIt1.minusDays(1L)
              }
            } else {
              null
            }
          }
          insertedLocations.last().id -> {
            getSunriseSunsetWithNowTimestamps(dateIt2, location.id).also {
              dateIt2 = dateIt2.minusDays(1L)
            }
          }
          else -> throw IllegalArgumentException()
        }
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
    assertEquals(expected = insertedLocations.size * limit, actual = remainingSunriseSunsets.size)
    remainingSunriseSunsets.groupBy(RemainingSunriseSunset::locationId).forEach {
      (locationId, sunriseSunsetsForLocation) ->
      assertEquals(expected = limit, actual = sunriseSunsetsForLocation.size)
      assertTrue {
        insertedSunriseSunsets
          .filter { it.locationId == locationId }
          .map(SunriseSunsetEntity::date)
          .sortedDescending()
          .containsAll(sunriseSunsetsForLocation.map(RemainingSunriseSunset::date))
      }
    }
  }

  @Test
  fun selectMostRecentForEachLocationId() = runTest {
    val limit = 2
    val insertedLocations = insertLocations(amount = 3)
    var dateIt1 = LocalDate.now()
    var dateIt2 = dateIt1.minusDays(10L)
    val dateIt3 = dateIt1.minusDays(20L)
    val insertedSunriseSunsets =
      insertSunriseSunsets(locations = insertedLocations, iterationsForEachLocation = 10) {
        location,
        sunriseSunsetIndex ->
        when (location.id) {
          insertedLocations[0].id -> {
            if (sunriseSunsetIndex % 2 == 0) {
              getSunriseSunsetWithNowTimestamps(dateIt1, location.id).also {
                dateIt1 = dateIt1.minusDays(1L)
              }
            } else {
              null
            }
          }
          insertedLocations[1].id -> {
            getSunriseSunsetWithNowTimestamps(dateIt2, location.id).also {
              dateIt2 = dateIt2.minusDays(1L)
            }
          }
          insertedLocations[2].id -> {
            if (sunriseSunsetIndex == 0) {
              getSunriseSunsetWithNowTimestamps(dateIt3, location.id)
            } else {
              null
            }
          }
          else -> throw IllegalArgumentException()
        }
      }

    val mostRecentSunriseSunsets = sunriseSunsetDao.selectMostRecentForEachLocation(limit = limit)
    assertEquals(expected = insertedLocations.size, actual = mostRecentSunriseSunsets.size)
    mostRecentSunriseSunsets.forEach { (location, sunriseSunsets) ->
      assertEquals(
        expected = if (location.id == insertedLocations.last().id) 1 else limit,
        actual = sunriseSunsets.size
      )
      assertTrue {
        insertedSunriseSunsets
          .filter { it.locationId == location.id }
          .map(SunriseSunsetEntity::date)
          .sortedDescending()
          .containsAll(sunriseSunsets.map(SunriseSunsetEntity::date))
      }
      assertTrue {
        sunriseSunsets
          .map(SunriseSunsetEntity::date)
          .contains(
            insertedSunriseSunsets
              .filter { it.locationId == location.id }
              .maxOf(SunriseSunsetEntity::date)
          )
      }
    }
  }

  private suspend fun insertLocations(
    amount: Int,
    latitude: (Int) -> Double = { 0.0 },
    longitude: (Int) -> Double = { 0.0 },
    zoneId: (Int) -> ZoneId = { ZoneId.systemDefault() }
  ): List<LocationEntity> {
    repeat(amount) { index ->
      locationDao.insert(
        latitude = latitude(index),
        longitude = longitude(index),
        zoneId = zoneId(index)
      )
    }
    return locationDao.selectAll()
  }

  private suspend fun insertSunriseSunsets(
    locations: List<LocationEntity>,
    iterationsForEachLocation: Int,
    sunriseSunset: (location: LocationEntity, sunriseSunsetIndex: Int) -> SunriseSunsetEntity?,
  ): List<SunriseSunsetEntity> {
    val insertedSunriseSunsets = mutableListOf<SunriseSunsetEntity>()
    repeat(iterationsForEachLocation) { index ->
      locations.forEach { location ->
        sunriseSunset(location, index)?.let {
          sunriseSunsetDao.insert(it)
          insertedSunriseSunsets.add(it)
        }
      }
    }
    return insertedSunriseSunsets
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
