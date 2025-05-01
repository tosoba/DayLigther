package com.trm.daylighter.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceDataSourceImpl
@Inject
constructor(@ApplicationContext private val context: Context) : PreferenceDataSource {
  private val geocodingEmailKey = stringPreferencesKey(PreferencesDataStoreKeys.GEOCODING_EMAIL)

  override fun isGeocodingEmailPreferenceSetFlow(): Flow<Boolean> =
    context.preferencesDataStore.data.map { preferences ->
      !preferences[geocodingEmailKey].isNullOrBlank()
    }

  override suspend fun clearGeocodingEmailPreference() {
    context.preferencesDataStore.edit { preferences -> preferences.remove(geocodingEmailKey) }
  }

  override suspend fun getGeocodingEmail(): String? =
    context.preferencesDataStore.data.map { preferences -> preferences[geocodingEmailKey] }.first()

  override fun getGeocodingEmailFlow(): Flow<String?> =
    context.preferencesDataStore.data.map { preferences -> preferences[geocodingEmailKey] }

  override suspend fun setGeocodingEmail(email: String) {
    context.preferencesDataStore.edit { preferences -> preferences[geocodingEmailKey] = email }
  }
}
