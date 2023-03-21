package com.trm.daylighter.di

import android.content.Context
import android.content.Intent
import com.trm.daylighter.MainActivity
import com.trm.daylighter.core.common.di.MainActivityIntent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
  @Provides
  @MainActivityIntent
  fun mainActivityIntent(@ApplicationContext context: Context): Intent =
    Intent(context, MainActivity::class.java)
}
