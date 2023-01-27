package com.trm.daylighter.domain.usecase

import com.trm.daylighter.domain.repo.LocationRepo
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import javax.inject.Inject

class SaveLocationUseCase
@Inject
constructor(
  private val locationRepo: LocationRepo,
  private val sunriseSunsetRepo: SunriseSunsetRepo,
) {
  suspend operator fun invoke(latitude: Double, longitude: Double) {
    locationRepo.saveLocation(latitude = latitude, longitude = longitude)
    sunriseSunsetRepo.enqueueSync()
  }
}
