package com.trm.daylighter.core.domain.usecase

fun interface SendLocationSavedEventUseCase {
  suspend operator fun invoke(id: Long)
}
