package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
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
  onAddLocation: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = emptyList())
  DayScreen(locations = locations.value, modifier = modifier, onAddLocation = onAddLocation)
}

@Composable
private fun DayScreen(
  locations: List<Location>,
  onAddLocation: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    if (locations.isEmpty()) {
      Button(onClick = onAddLocation, modifier = Modifier.align(Alignment.Center)) {
        Text(text = "Add location")
      }
    } else {
      Text(
        text = """${locations.first().latitude}, ${locations.first().longitude}""",
        modifier = Modifier.align(Alignment.Center)
      )
    }
  }
}
