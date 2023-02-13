package com.trm.daylighter.domain.usecase

import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.repo.LocationRepo
import javax.inject.Inject

class GetLocationById @Inject constructor(private val repo: LocationRepo) {
  suspend operator fun invoke(id: Long): Location = repo.getLocationById(id)
}
