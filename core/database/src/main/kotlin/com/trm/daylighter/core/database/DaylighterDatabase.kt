package com.trm.daylighter.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trm.daylighter.core.database.converter.LocalDateConverter
import com.trm.daylighter.core.database.converter.ZoneIdConverter
import com.trm.daylighter.core.database.converter.ZonedDateTimeConverter
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.dao.SunriseSunsetDao
import com.trm.daylighter.core.database.entity.LocationEntity
import com.trm.daylighter.core.database.entity.SunriseSunsetEntity

@Database(
  entities = [SunriseSunsetEntity::class, LocationEntity::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(ZonedDateTimeConverter::class, LocalDateConverter::class, ZoneIdConverter::class)
abstract class DaylighterDatabase : RoomDatabase() {
  abstract fun sunriseSunsetDao(): SunriseSunsetDao
  abstract fun locationDao(): LocationDao
}
