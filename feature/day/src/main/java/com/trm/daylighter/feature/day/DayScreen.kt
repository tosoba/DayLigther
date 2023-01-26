package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.domain.model.Location

const val dayRoute = "day_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DayRoute(
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = emptyList())
  DayScreen(locations = locations.value, modifier = modifier)
}

@Composable
private fun DayScreen(
  locations: List<Location>,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    Text(
      text =
        if (locations.isEmpty()) "No locations"
        else """${locations.first().latitude}, ${locations.first().longitude}""",
      modifier = Modifier.align(Alignment.Center)
    )
  }
}
