package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject

class GetLocationAtIndexUseCase @Inject constructor(private val repo: LocationRepo) {
  suspend operator fun invoke(index: Int): Location? = repo.getLocationAtIndex(index)
}
