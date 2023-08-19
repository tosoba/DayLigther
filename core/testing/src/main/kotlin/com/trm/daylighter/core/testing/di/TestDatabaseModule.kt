package com.trm.daylighter.core.testing.di

import android.content.Context
import androidx.room.Room
import com.trm.daylighter.core.database.DayLighterDatabase
import com.trm.daylighter.core.database.dao.LocationDao
import com.trm.daylighter.core.database.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {
  @Provides
  @Singleton
  fun dayLighterDatabase(@ApplicationContext context: Context): DayLighterDatabase =
    Room.inMemoryDatabaseBuilder(context, DayLighterDatabase::class.java)
      .allowMainThreadQueries()
      .build()

  @Provides fun locationDao(db: DayLighterDatabase): LocationDao = db.locationDao()
}
