package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(private val locationRepo: LocationRepo) {
  suspend operator fun invoke(latitude: Double, longitude: Double) {
    locationRepo.saveLocation(latitude = latitude, longitude = longitude)
  }

  suspend operator fun invoke(id: Long, latitude: Double, longitude: Double) {
    locationRepo.updateLocationLatLngById(id = id, latitude = latitude, longitude = longitude)
  }
}
