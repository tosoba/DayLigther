package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.LatLng

fun interface GetCurrentUserLatLngUseCase {
  suspend operator fun invoke(): LatLng?
}
