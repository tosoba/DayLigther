package com.trm.daylighter.core.database.di

import android.content.Context
import androidx.room.Room
import com.trm.daylighter.core.database.DayLighterDatabase
import com.trm.daylighter.core.database.dao.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Provides
  @Singleton
  fun dayLighterDatabase(@ApplicationContext context: Context): DayLighterDatabase =
    Room.databaseBuilder(context, DayLighterDatabase::class.java, DayLighterDatabase.DB_NAME)
      .build()

  @Provides fun locationDao(db: DayLighterDatabase): LocationDao = db.locationDao()
}
