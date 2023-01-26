package com.trm.daylighter.locations

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

const val locationsRoute = "locations_route"

@Composable
fun LocationsScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier) { Text(text = "Locations", modifier = Modifier.align(Alignment.Center)) }
}
