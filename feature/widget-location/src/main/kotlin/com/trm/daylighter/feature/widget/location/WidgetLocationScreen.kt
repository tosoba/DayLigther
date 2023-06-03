package com.trm.daylighter.feature.widget.location

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.MapDefaults
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.WithData
import com.trm.daylighter.core.domain.model.WithoutData
import com.trm.daylighter.core.ui.composable.DisabledMapView
import com.trm.daylighter.core.ui.composable.InfoButtonCard
import com.trm.daylighter.core.ui.composable.LocationNameGradientOverlay
import com.trm.daylighter.core.ui.composable.LocationNameLabel
import com.trm.daylighter.core.ui.composable.MarkerIcon
import com.trm.daylighter.core.ui.composable.ZoomControlsRow
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
    onLocationSelected = { viewModel.selectedLocationId = it },
    mode = viewModel.mode,
    onConfirmLocationSelectionClick = {},
    onAddLocationClick = {},
    modifier = modifier
  )
}

@Composable
private fun WidgetLocationScreen(
  locations: Loadable<List<StableValue<Location>>>,
  selectedLocationId: Long?,
  onLocationSelected: (Long) -> Unit,
  mode: WidgetLocationMode,
  onConfirmLocationSelectionClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var zoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

    val bottomButtonsPaddingDp = 20.dp
    var addWidgetButtonHeightPx by remember { mutableStateOf(0) }
    var zoomButtonsRowHeightPx by remember { mutableStateOf(0) }

    when (locations) {
      is WithData -> {
        if (locations.data.isNotEmpty()) {
          val columnsCount =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 2
            else 4
          LazyVerticalGrid(
            contentPadding = PaddingValues(10.dp),
            columns = GridCells.Fixed(columnsCount)
          ) {
            items(locations.data, key = { it.value.id }) { location ->
              MapCard(
                location = location,
                zoom = zoom,
                isSelected = location.value.id == selectedLocationId,
                onSelected = onLocationSelected
              )
            }

            val spacerHeightPx = maxOf(addWidgetButtonHeightPx, zoomButtonsRowHeightPx)
            if (spacerHeightPx > 0) {
              item(span = { GridItemSpan(columnsCount) }) {
                Spacer(
                  modifier =
                    Modifier.height(
                      bottomButtonsPaddingDp * 2 +
                        with(LocalDensity.current) { spacerHeightPx.toDp() }
                    )
                )
              }
            }
          }

          ZoomControlsRow(
            zoom = zoom,
            incrementZoom = { ++zoom },
            decrementZoom = { --zoom },
            modifier =
              Modifier.align(Alignment.BottomStart)
                .padding(bottomButtonsPaddingDp)
                .onGloballyPositioned { zoomButtonsRowHeightPx = it.size.height }
          )

          AnimatedVisibility(
            visible = selectedLocationId != null,
            modifier =
              Modifier.align(Alignment.BottomEnd)
                .padding(bottomButtonsPaddingDp)
                .onGloballyPositioned { addWidgetButtonHeightPx = it.size.height }
          ) {
            FloatingActionButton(onClick = onConfirmLocationSelectionClick) {
              Icon(
                imageVector =
                  when (mode) {
                    WidgetLocationMode.ADD -> Icons.Filled.Add
                    WidgetLocationMode.EDIT -> Icons.Filled.Done
                  },
                contentDescription =
                  stringResource(
                    id =
                      when (mode) {
                        WidgetLocationMode.ADD -> R.string.add_a_widget
                        WidgetLocationMode.EDIT -> R.string.update_a_widget
                      }
                  )
              )
            }
          }
        } else {
          InfoButtonCard(
            infoText = stringResource(commonR.string.no_saved_locations_add_one),
            actionText = stringResource(commonR.string.add_location),
            onButtonClick = onAddLocationClick,
            modifier = Modifier.align(Alignment.Center).padding(20.dp)
          )
          Text(
            text = stringResource(commonR.string.no_locations),
            modifier = Modifier.align(Alignment.Center)
          )
        }
      }
      is WithoutData -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  }
}

@Composable
private fun MapCard(
  location: StableValue<Location>,
  zoom: Double,
  isSelected: Boolean,
  onSelected: (Long) -> Unit
) {
  Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(5.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      DisabledMapView(
        latitude = location.value.latitude,
        longitude = location.value.longitude,
        zoom = zoom
      )

      LocationNameGradientOverlay()

      MarkerIcon(modifier = Modifier.align(Alignment.Center).size(36.dp))

      LocationNameLabel(
        name = location.value.name,
        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(5.dp)
      )

      Checkbox(
        checked = isSelected,
        onCheckedChange = { if (it) onSelected(location.value.id) },
        modifier = Modifier.align(Alignment.TopEnd)
      )
    }
  }
}
