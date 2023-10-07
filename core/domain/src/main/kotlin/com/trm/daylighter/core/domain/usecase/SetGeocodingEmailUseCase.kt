package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import javax.inject.Inject

class SetGeocodingEmailUseCase @Inject constructor(private val dataSource: PreferenceDataSource) {
  suspend operator fun invoke(email: String) {
    dataSource.setGeocodingEmail(email)
  }
}
