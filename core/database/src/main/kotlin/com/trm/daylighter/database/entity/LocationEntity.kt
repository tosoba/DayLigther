package com.trm.daylighter.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationEntity(
  @PrimaryKey var id: Long = 0,
  val latitude: Double,
  val longitude: Double,
  @ColumnInfo(name = "is_default") val isDefault: Boolean,
)
