package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetLocationAtIndexFlowUseCase @Inject constructor(private val repo: LocationRepo) {
  operator fun invoke(index: Int): Flow<Location?> = repo.getLocationAtIndexFlow(index)
}
