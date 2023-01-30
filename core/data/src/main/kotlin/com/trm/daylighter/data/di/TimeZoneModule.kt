package com.trm.daylighter.data.di

import com.trm.daylighter.core.common.di.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.iakovlev.timeshape.TimeZoneEngine

@Module
@InstallIn(SingletonComponent::class)
class TimeZoneModule {
  @Provides
  @Singleton
  fun timeZoneEngine(@ApplicationScope scope: CoroutineScope): TimeZoneEngineAsyncProvider =
    TimeZoneEngineAsyncProvider(scope.async { TimeZoneEngine.initialize() })
}

class TimeZoneEngineAsyncProvider(val engine: Deferred<TimeZoneEngine>)
