package com.trm.daylighter.core.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ReceiveLocationSavedEventUseCase {
  operator fun invoke(): Flow<Long>
}
