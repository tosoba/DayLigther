package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(private val locationRepo: LocationRepo) {
  suspend operator fun invoke(latitude: Double, longitude: Double) {
    locationRepo.saveLocation(latitude = latitude, longitude = longitude)
  }
}
