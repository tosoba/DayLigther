package com.trm.daylighter.core.database.dao

import androidx.room.*
import com.trm.daylighter.core.database.entity.LocationEntity
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

  @Query("DELETE FROM location WHERE id = :id") suspend fun deleteById(id: Long)

  @Query("SELECT COUNT(*) FROM location") suspend fun selectCountAll(): Int

  @Query("SELECT COUNT(*) FROM location") fun selectCountAllFlow(): Flow<Int>

  @Transaction
  suspend fun deleteByIdAndSelectCountAll(id: Long, isDefault: Boolean): Int {
    deleteById(id)
    if (isDefault) setDefaultToMostRecentlyAddedLocation()
    return selectCountAll()
  }

  @Query("SELECT * FROM location WHERE is_default = TRUE")
  fun selectDefaultFlow(): Flow<LocationEntity?>

  @Query("SELECT EXISTS(SELECT * FROM location)") suspend fun selectAnyExists(): Boolean

  @Query("SELECT * FROM location") suspend fun selectAll(): List<LocationEntity>

  @Query("SELECT * FROM location") fun selectAllFlow(): Flow<List<LocationEntity>>

  @Query("SELECT * FROM location WHERE id = :id") suspend fun selectById(id: Long): LocationEntity

  @Query("UPDATE location SET is_default = FALSE") suspend fun setIsDefaultToFalse()

  @Query("UPDATE location SET is_default = TRUE WHERE id = :id")
  suspend fun setIsDefaultToTrueById(id: Long)

  @Transaction
  suspend fun updateDefaultLocationById(id: Long) {
    setIsDefaultToFalse()
    setIsDefaultToTrueById(id)
  }

  @Query(
    "UPDATE location SET is_default = TRUE WHERE updated_at = (SELECT MAX(updated_at) FROM location)"
  )
  suspend fun setDefaultToMostRecentlyAddedLocation()

  @Query(
    "UPDATE location SET latitude = :latitude, longitude = :longitude, zone_id = :zoneId WHERE id = :id"
  )
  suspend fun updateLocationLatLngById(
    id: Long,
    latitude: Double,
    longitude: Double,
    zoneId: ZoneId
  )

  @Query("SELECT * FROM location ORDER BY is_default DESC, updated_at DESC LIMIT 1 OFFSET :offset")
  suspend fun selectLocationAtOffset(offset: Int): LocationEntity?
}
