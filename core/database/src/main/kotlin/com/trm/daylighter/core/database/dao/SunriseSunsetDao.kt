package com.trm.daylighter.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.daylighter.core.database.entity.SunriseSunsetEntity
import java.time.LocalDate

@Dao
interface SunriseSunsetDao {
  @Query("SELECT * FROM sunrise_sunset WHERE location_id = :locationId AND date = :date")
  suspend fun selectByLocationIdAndDate(locationId: Long, date: LocalDate): SunriseSunsetEntity

  @Query(
    "SELECT * FROM sunrise_sunset WHERE location_id = :locationId ORDER BY date DESC LIMIT :limit"
  )
  suspend fun selectMostRecentByLocationId(locationId: Long, limit: Int): List<SunriseSunsetEntity>

  @Query(
    "SELECT ss.* FROM location l " +
      "INNER JOIN sunrise_sunset ss ON ss.location_id = l.id " +
      "AND ss.date IN (SELECT date FROM sunrise_sunset ssi WHERE location_id = l.id ORDER BY date DESC LIMIT :limit) " +
      "WHERE ss.location_id IN (:locationIds)"
  )
  suspend fun selectMostRecentForEachLocationId(
    locationIds: Collection<Long>,
    limit: Int
  ): List<SunriseSunsetEntity>

  @Upsert suspend fun insert(entity: SunriseSunsetEntity)

  @Upsert suspend fun insertMany(entities: Collection<SunriseSunsetEntity>)

  @Query("DELETE FROM sunrise_sunset WHERE location_id = :locationId")
  suspend fun deleteByLocationId(locationId: Long)

  @Query(
    "DELETE FROM sunrise_sunset WHERE rowid IN (" +
      "SELECT ss.rowid FROM location l " +
      "INNER JOIN sunrise_sunset ss ON ss.location_id = l.id " +
      "AND ss.date NOT IN (SELECT date FROM sunrise_sunset ssi WHERE location_id = l.id ORDER BY date DESC LIMIT :limit))"
  )
  suspend fun deleteForEachLocationExceptMostRecent(limit: Int)
}
