package com.trm.daylighter.widget.location

import androidx.datastore.preferences.core.MutablePreferences
import java.util.UUID

internal fun updateUuid(prefs: MutablePreferences) {
  prefs[uuidKey] = UUID.randomUUID().toString()
}

internal class WidgetLocationIdUpdate(private val locationId: Long) : (MutablePreferences) -> Unit {
  override fun invoke(prefs: MutablePreferences) {
    prefs[locationIdKey] = locationId
    prefs[uuidKey] = UUID.randomUUID().toString()
  }
}
