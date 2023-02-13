package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject

class SetDefaultLocationUseCase @Inject constructor(private val repo: LocationRepo) {
  suspend operator fun invoke(id: Long) {
    repo.setDefaultLocationById(id)
  }
}
