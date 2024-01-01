package com.trm.daylighter.core.database.dao

import androidx.room.*
import com.trm.daylighter.core.database.entity.LocationEntity
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
  @Insert suspend fun insert(entity: LocationEntity)

  @Transaction
  suspend fun insert(latitude: Double, longitude: Double, name: String, zoneId: ZoneId) {
    insert(
      LocationEntity(
        latitude = latitude,
        longitude = longitude,
        name = name,
        isDefault = !selectAnyExists(),
        updatedAt = LocalDateTime.now(),
        zoneId = zoneId,
      )
    )
  }

  @Query("DELETE FROM location") suspend fun deleteAll()

  @Query("DELETE FROM location WHERE id = :id") suspend fun deleteById(id: Long)

  @Transaction
  suspend fun deleteById(id: Long, isDefault: Boolean) {
    deleteById(id)
    if (isDefault) setDefaultToMostRecentlyAddedLocation()
  }

  @Query("SELECT * FROM location WHERE is_default = 1")
  fun selectDefaultFlow(): Flow<LocationEntity?>

  @Query("SELECT * FROM location WHERE is_default = 1") fun selectDefault(): LocationEntity?

  @Query("SELECT EXISTS(SELECT * FROM location)") suspend fun selectAnyExists(): Boolean

  @Query("SELECT * FROM location ORDER BY is_default DESC, updated_at DESC")
  fun selectAllFlow(): Flow<List<LocationEntity>>

  @Query("SELECT * FROM location WHERE id = :id") suspend fun selectById(id: Long): LocationEntity?

  @Query("UPDATE location SET is_default = 0") suspend fun setIsDefaultToFalse()

  @Query("UPDATE location SET is_default = 1 WHERE id = :id")
  suspend fun setIsDefaultToTrueById(id: Long)

  @Transaction
  suspend fun updateDefaultLocationById(id: Long) {
    setIsDefaultToFalse()
    setIsDefaultToTrueById(id)
  }

  @Query(
    "UPDATE location SET is_default = 1 WHERE updated_at = (SELECT MAX(updated_at) FROM location)"
  )
  suspend fun setDefaultToMostRecentlyAddedLocation()

  @Query(
    "UPDATE location SET latitude = :latitude, longitude = :longitude, name = :name, zone_id = :zoneId WHERE id = :id"
  )
  suspend fun updateLocationLatLngById(
    id: Long,
    latitude: Double,
    longitude: Double,
    name: String,
    zoneId: ZoneId
  )

  @Query(
    "SELECT position " +
      "FROM (SELECT id, (SELECT COUNT(*) FROM location WHERE updated_at > l.updated_at AND is_default = 0) + 1 AS position " +
      "FROM location l WHERE is_default = 0 ORDER BY updated_at DESC) WHERE id = :id"
  )
  fun selectNonDefaultLocationIndexById(id: Long): Int?
}
