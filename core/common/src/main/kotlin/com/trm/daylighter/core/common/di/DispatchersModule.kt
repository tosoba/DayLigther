package com.trm.daylighter.core.common.di

import com.trm.daylighter.core.domain.di.DaylighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
