package com.trm.daylighter.core.domain.repo

import kotlinx.coroutines.flow.Flow

interface PreferenceDataSource {
  fun isGeocodingEmailPreferenceSetFlow(): Flow<Boolean>
}
