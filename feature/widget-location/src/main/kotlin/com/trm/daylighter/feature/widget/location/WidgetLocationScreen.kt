package com.trm.daylighter.feature.widget.location

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.ui.model.StableValue

const val newWidgetRoute = "widget_location_route"

@Composable
fun WidgetLocationRoute(
  modifier: Modifier = Modifier,
  viewModel: WidgetLocationViewModel = hiltViewModel()
) {
  val locations = viewModel.locations.collectAsState(initial = Empty)
  val selectedLocationId = viewModel.selectedLocationIdFlow.collectAsState()
  WidgetLocationScreen(
    locations = locations.value,
    selectedLocationId = selectedLocationId.value,
    modifier = modifier
  )
}

@Composable
private fun WidgetLocationScreen(
  locations: Loadable<List<StableValue<Location>>>,
  selectedLocationId: Long?,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    Text(text = "Widget location", modifier = Modifier.align(Alignment.Center))
  }
}
