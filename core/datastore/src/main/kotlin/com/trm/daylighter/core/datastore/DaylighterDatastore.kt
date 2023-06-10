package com.trm.daylighter.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val DATASTORE_PREFERENCES_NAME = "Preferences"

val Context.preferencesDataStore: DataStore<Preferences> by
  preferencesDataStore(name = DATASTORE_PREFERENCES_NAME)
