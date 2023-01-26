package com.trm.daylighter.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

const val widgetsRoute = "widgets_route"

@Composable
fun WidgetsScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text(text = "Widgets", modifier = Modifier.align(Alignment.Center)) }
}
