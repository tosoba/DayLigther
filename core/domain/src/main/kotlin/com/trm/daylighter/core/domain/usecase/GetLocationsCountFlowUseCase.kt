package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetLocationsCountFlowUseCase @Inject constructor(private val repo: LocationRepo) {
  operator fun invoke(): Flow<Int> = repo.getLocationsCountFlow()
}
