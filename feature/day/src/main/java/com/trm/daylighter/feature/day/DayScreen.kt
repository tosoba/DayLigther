package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.domain.model.Empty
import com.trm.daylighter.domain.model.Loadable
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.model.WithData

const val dayRoute = "day_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DayRoute(
  onAddLocation: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = Empty)
  DayScreen(
    locationsLoadable = locations.value,
    onAddLocationClick = onAddLocation,
    modifier = modifier
  )
}

@Composable
private fun DayScreen(
  locationsLoadable: Loadable<List<Location>>,
  onAddLocationClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    when (locationsLoadable) {
      is WithData -> {
        if (locationsLoadable.data.isEmpty()) {
          Button(onClick = onAddLocationClick, modifier = Modifier.align(Alignment.Center)) {
            Text(text = "Add location")
          }
        } else {
          Text(
            text =
              """${locationsLoadable.data.first().latitude}, ${locationsLoadable.data.first().longitude}""",
            modifier = Modifier.align(Alignment.Center)
          )
        }
      }
      else -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  }
}
