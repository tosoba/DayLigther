package com.trm.daylighter.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceDataSourceImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : PreferenceDataSource {
  override fun isGeocodingEmailPreferenceSetFlow(): Flow<Boolean> =
    context.preferencesDataStore.data.map { preferences ->
      !preferences[stringPreferencesKey(PreferencesDataStoreKeys.GEOCODING_EMAIL)].isNullOrBlank()
    }

  override suspend fun clearGeocodingEmailPreference() {
    context.preferencesDataStore.edit { preferences ->
      preferences.remove(stringPreferencesKey(PreferencesDataStoreKeys.GEOCODING_EMAIL))
    }
  }
}
