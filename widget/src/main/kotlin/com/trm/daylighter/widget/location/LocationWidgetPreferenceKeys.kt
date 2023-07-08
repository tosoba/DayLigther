package com.trm.daylighter.widget.location

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal val locationIdKey: Preferences.Key<Long>
  get() = longPreferencesKey("location_id")

internal val uuidKey: Preferences.Key<String>
  get() = stringPreferencesKey("uuid")
