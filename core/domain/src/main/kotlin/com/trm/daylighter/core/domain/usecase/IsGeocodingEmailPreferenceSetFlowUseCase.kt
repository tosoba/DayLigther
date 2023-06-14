package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class IsGeocodingEmailPreferenceSetFlowUseCase
@Inject
constructor(private val dataSource: PreferenceDataSource) {
  operator fun invoke(): Flow<Boolean> = dataSource.isGeocodingEmailPreferenceSetFlow()
}
