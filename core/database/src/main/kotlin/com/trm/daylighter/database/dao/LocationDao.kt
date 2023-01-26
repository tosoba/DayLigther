package com.trm.daylighter.database.dao

import androidx.room.*
import com.trm.daylighter.database.entity.LocationEntity

@Dao
interface LocationDao {
  @Insert suspend fun insert(entity: LocationEntity)

  @Update suspend fun update(entity: LocationEntity)

  @Delete suspend fun delete(entity: LocationEntity)

  @Query("SELECT * FROM location WHERE is_default = TRUE")
  suspend fun selectDefault(): LocationEntity?

  @Query("SELECT * FROM location") suspend fun selectAll(): List<LocationEntity>
}
