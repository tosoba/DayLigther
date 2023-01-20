package com.trm.daylighter.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.daylighter.database.entity.SunriseSunsetEntity
import java.time.LocalDate

@Dao
interface SunriseSunsetDao {
  @Query("SELECT * FROM sunrise_sunset WHERE date = :date")
  suspend fun selectByDate(date: LocalDate): SunriseSunsetEntity

  @Query("SELECT * FROM sunrise_sunset WHERE date IN (:dates)")
  suspend fun selectByDates(dates: List<LocalDate>): List<SunriseSunsetEntity>

  @Upsert suspend fun insert(entity: SunriseSunsetEntity)

  @Upsert suspend fun insertMany(entities: List<SunriseSunsetEntity>)
}
