package com.trm.daylighter.feature.widget.location

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

const val newWidgetRoute = "widget_location_route"

@Composable
fun WidgetLocationScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    Text(text = "Widget location", modifier = Modifier.align(Alignment.Center))
  }
}
