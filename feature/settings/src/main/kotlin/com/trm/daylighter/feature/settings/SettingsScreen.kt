package com.trm.daylighter.feature.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.jamal.composeprefs3.ui.GroupHeader
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.*
import com.trm.daylighter.core.datastore.preferencesDataStore

const val settingsRoute = "settings_route"

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
  PrefsScreen(dataStore = LocalContext.current.preferencesDataStore, modifier = modifier) {
    prefsGroup({ GroupHeader(title = "TextPref", color = MaterialTheme.colorScheme.secondary) }) {
      prefsItem { TextPref(title = "Just some text") }
      prefsItem { TextPref(title = "Just some text", summary = "But now with a summary") }
    }
  }
}
