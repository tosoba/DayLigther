package com.trm.daylighter.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.daylighter.database.entity.SunriseSunsetEntity
import java.time.LocalDate

@Dao
interface SunriseSunsetDao {
  @Query("SELECT * FROM sunrise_sunset WHERE location_id = :locationId AND date = :date")
  suspend fun selectByLocationIdAndDate(locationId: Long, date: LocalDate): SunriseSunsetEntity

  @Query("SELECT * FROM sunrise_sunset WHERE location_id IN (:locationIds) AND date IN (:dates)")
  suspend fun selectByLocationIdsAndDates(
    locationIds: Collection<Long>,
    dates: Collection<LocalDate>
  ): List<SunriseSunsetEntity>

  @Upsert suspend fun insert(entity: SunriseSunsetEntity)

  @Upsert suspend fun insertMany(entities: Collection<SunriseSunsetEntity>)
}
