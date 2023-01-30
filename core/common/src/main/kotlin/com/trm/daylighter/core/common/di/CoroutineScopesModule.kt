package com.trm.daylighter.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@InstallIn(SingletonComponent::class)
@Module
object CoroutineScopesModule {
  @Provides
  @Singleton
  @ApplicationScope
  fun providesCoroutineScope(
    @Dispatcher(DaylighterDispatchers.DEFAULT) defaultDispatcher: CoroutineDispatcher
  ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
}

@Retention(AnnotationRetention.RUNTIME) @Qualifier annotation class ApplicationScope
