package com.trm.daylighter.database.di

import android.content.Context
import androidx.room.Room
import com.trm.daylighter.database.DaylighterDatabase
import com.trm.daylighter.database.dao.SunriseSunsetDao
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
  fun daylighterDatabase(@ApplicationContext context: Context): DaylighterDatabase =
    Room.databaseBuilder(context, DaylighterDatabase::class.java, "daylighter.db").build()

  @Provides fun sunriseSunsetDao(db: DaylighterDatabase): SunriseSunsetDao = db.sunriseSunsetDao()
}
