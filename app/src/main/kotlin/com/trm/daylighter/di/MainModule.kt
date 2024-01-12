package com.trm.daylighter.di

import com.trm.daylighter.MainActivity
import com.trm.daylighter.core.common.di.provider.ClassProvider
import com.trm.daylighter.core.common.di.provider.MainActivityClassProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
  @Provides
  @MainActivityClassProvider
  fun mainActivityClassProvider(): ClassProvider = ClassProvider(MainActivity::class.java)
}
