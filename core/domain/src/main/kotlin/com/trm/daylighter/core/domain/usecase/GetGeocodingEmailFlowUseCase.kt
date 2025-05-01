package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetGeocodingEmailFlowUseCase
@Inject
constructor(private val dataSource: PreferenceDataSource) {
  operator fun invoke(): Flow<String?> = dataSource.getGeocodingEmailFlow()
}
