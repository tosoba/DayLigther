package com.trm.daylighter.database.dao

import androidx.room.*
import com.trm.daylighter.database.entity.LocationEntity
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
  @Insert suspend fun insert(entity: LocationEntity)

  @Transaction
  suspend fun insert(latitude: Double, longitude: Double, zoneId: ZoneId) {
    val anyExists = selectAnyExists()
    insert(
      LocationEntity(
        latitude = latitude,
        longitude = longitude,
        isDefault = !anyExists,
        updatedAt = ZonedDateTime.now(),
        zoneId = zoneId,
      )
    )
  }

  @Update suspend fun update(entity: LocationEntity)

  @Delete suspend fun delete(entity: LocationEntity)

  @Query("DELETE FROM location WHERE id = :id") suspend fun deleteById(id: Long)

  @Query("SELECT COUNT(*) FROM location") suspend fun selectCountAll(): Int

  @Query("SELECT COUNT(*) FROM location") fun selectCountAllFlow(): Flow<Int>

  @Transaction
  suspend fun deleteByIdAndSelectCountAll(id: Long): Int {
    deleteById(id)
    return selectCountAll()
  }

  @Query("SELECT * FROM location WHERE is_default = TRUE")
  fun selectDefaultFlow(): Flow<LocationEntity?>

  @Query("SELECT EXISTS(SELECT * FROM location)") suspend fun selectAnyExists(): Boolean

  @Query("SELECT * FROM location") suspend fun selectAll(): List<LocationEntity>

  @Query("SELECT * FROM location") fun selectAllFlow(): Flow<List<LocationEntity>>

  @Query("SELECT * FROM location WHERE id = :id") suspend fun selectById(id: Long): LocationEntity
}
