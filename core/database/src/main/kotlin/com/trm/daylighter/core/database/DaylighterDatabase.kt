package com.trm.daylighter.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trm.daylighter.core.database.converter.LocalDateTimeConverter
import com.trm.daylighter.core.database.converter.ZoneIdConverter
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.entity.LocationEntity

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class, ZoneIdConverter::class)
abstract class DaylighterDatabase : RoomDatabase() {
  abstract fun locationDao(): LocationDao
}
