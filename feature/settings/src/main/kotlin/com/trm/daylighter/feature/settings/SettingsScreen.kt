package com.trm.daylighter.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

const val settingsRoute = "settings_route"

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text(text = "Settings", modifier = Modifier.align(Alignment.Center)) }
}
