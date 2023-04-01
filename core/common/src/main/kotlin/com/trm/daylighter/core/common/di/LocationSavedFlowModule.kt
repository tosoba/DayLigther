package com.trm.daylighter.core.common.di

import com.trm.daylighter.core.domain.usecase.ReceiveLocationSavedEventUseCase
import com.trm.daylighter.core.domain.usecase.SendLocationSavedEventUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Module
@InstallIn(SingletonComponent::class)
object LocationSavedFlowModule {
  private val locationSavedFlow = MutableSharedFlow<Long>()

  @Provides
  fun sendLocationSavedEventUseCase() = SendLocationSavedEventUseCase(locationSavedFlow::emit)

  @Provides
  fun receiveLocationSavedEventUseCase() = ReceiveLocationSavedEventUseCase {
    locationSavedFlow.asSharedFlow()
  }
}
