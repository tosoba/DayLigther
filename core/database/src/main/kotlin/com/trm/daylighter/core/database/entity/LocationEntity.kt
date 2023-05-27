package com.trm.daylighter.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(tableName = "location")
data class LocationEntity(
  @PrimaryKey(autoGenerate = true) var id: Long = 0,
  val latitude: Double,
  val longitude: Double,
  val name: String,
  @ColumnInfo(name = "is_default") val isDefault: Boolean,
  @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime,
  @ColumnInfo(name = "zone_id") val zoneId: ZoneId,
)
