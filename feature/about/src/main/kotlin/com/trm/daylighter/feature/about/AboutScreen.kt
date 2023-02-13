package com.trm.daylighter.feature.about

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

const val aboutRoute = "about_route"

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text(text = "About", modifier = Modifier.align(Alignment.Center)) }
}
