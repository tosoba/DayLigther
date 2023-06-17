package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import javax.inject.Inject

class ClearGeocodingEmailPreferenceUseCase
@Inject
constructor(private val dataSource: PreferenceDataSource) {
  suspend operator fun invoke() {
    dataSource.clearGeocodingEmailPreference()
  }
}
