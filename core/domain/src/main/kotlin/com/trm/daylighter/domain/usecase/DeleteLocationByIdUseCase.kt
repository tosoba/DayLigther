package com.trm.daylighter.domain.usecase

import com.trm.daylighter.domain.repo.LocationRepo
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import javax.inject.Inject

class DeleteLocationByIdUseCase
@Inject
constructor(
  private val locationRepo: LocationRepo,
  private val sunriseSunsetRepo: SunriseSunsetRepo,
) {
  suspend operator fun invoke(id: Long) {
    if (locationRepo.deleteLocationByIdAndGetCountAll(id) == 0) {
      sunriseSunsetRepo.cancelSync()
    }
  }
}
