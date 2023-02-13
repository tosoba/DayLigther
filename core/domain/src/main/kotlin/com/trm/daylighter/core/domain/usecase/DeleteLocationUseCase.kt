package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.repo.LocationRepo
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import javax.inject.Inject

class DeleteLocationUseCase
@Inject
constructor(
  private val locationRepo: LocationRepo,
  private val sunriseSunsetRepo: SunriseSunsetRepo,
) {
  suspend operator fun invoke(location: Location) {
    if (
      locationRepo.deleteLocationByIdAndGetCountAll(
        id = location.id,
        isDefault = location.isDefault
      ) == 0
    ) {
      sunriseSunsetRepo.cancelSync()
    }
  }
}
