package com.trm.daylighter.core.testing.di

import com.trm.daylighter.core.common.di.DispatchersModule
import com.trm.daylighter.core.domain.di.DayLighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

@Module
@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [DispatchersModule::class],
)
object TestDispatchersModule {
  @Provides
  @Dispatcher(DayLighterDispatchers.DEFAULT)
  fun defaultDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher

  @Provides
  @Dispatcher(DayLighterDispatchers.IO)
  fun ioDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher
}
