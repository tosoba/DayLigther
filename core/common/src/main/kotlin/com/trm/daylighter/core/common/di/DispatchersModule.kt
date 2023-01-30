package com.trm.daylighter.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
  @Provides
  @Dispatcher(DaylighterDispatchers.IO)
  fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

  @Provides
  @Dispatcher(DaylighterDispatchers.DEFAULT)
  fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: DaylighterDispatchers)

enum class DaylighterDispatchers {
  IO,
  DEFAULT
}
