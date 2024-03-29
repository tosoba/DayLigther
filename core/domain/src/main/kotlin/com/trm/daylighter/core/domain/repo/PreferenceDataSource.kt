package com.trm.daylighter.core.domain.repo

import kotlinx.coroutines.flow.Flow

interface PreferenceDataSource {
  fun isGeocodingEmailPreferenceSetFlow(): Flow<Boolean>

  suspend fun clearGeocodingEmailPreference()

  suspend fun getGeocodingEmail(): String?

  fun getGeocodingEmailFlow(): Flow<String?>

  suspend fun setGeocodingEmail(email: String)
}
