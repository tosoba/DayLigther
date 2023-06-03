package com.trm.daylighter.feature.widget.location

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

const val newWidgetRoute = "widget_location_route"

@Composable
fun WidgetLocationRoute(
  modifier: Modifier = Modifier,
  viewModel: WidgetLocationViewModel = hiltViewModel()
) {
  WidgetLocationScreen(modifier = modifier)
}

@Composable
private fun WidgetLocationScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    Text(text = "Widget location", modifier = Modifier.align(Alignment.Center))
  }
}
