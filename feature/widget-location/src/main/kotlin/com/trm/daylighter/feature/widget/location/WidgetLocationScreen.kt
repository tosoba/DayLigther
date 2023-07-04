package com.trm.daylighter.feature.widget.location

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.ui.composable.DayPeriodChart
import com.trm.daylighter.core.ui.composable.DisabledMapView
import com.trm.daylighter.core.ui.composable.DrawerMenuTopAppBar
import com.trm.daylighter.core.ui.composable.InfoButtonCard
import com.trm.daylighter.core.ui.composable.LocationNameGradientOverlay
import com.trm.daylighter.core.ui.composable.LocationNameLabel
import com.trm.daylighter.core.ui.composable.MarkerIcon
import com.trm.daylighter.core.ui.composable.ZoomControlsRow
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import kotlinx.coroutines.flow.collectLatest

const val newWidgetRoute = "widget_location_route"

@Composable
fun WidgetLocationRoute(
  modifier: Modifier = Modifier,
  onAddLocationClick: () -> Unit,
  onDrawerMenuClick: () -> Unit,
  viewModel: WidgetLocationViewModel = hiltViewModel()
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = LoadingFirst)
  val selectedLocationId = viewModel.selectedLocationIdFlow.collectAsStateWithLifecycle()

  WidgetLocationScreen(
    locations = locations.value,
    selectedLocationId = selectedLocationId.value,
    onLocationSelected = { viewModel.selectedLocationId = it },
    mode = viewModel.mode,
    onConfirmLocationSelectionClick = viewModel::confirmLocationSelection,
    onAddLocationClick = onAddLocationClick,
    onDrawerMenuClick = onDrawerMenuClick,
    modifier = modifier
  )

  val context = LocalContext.current
  var currentToast: Toast? by remember { mutableStateOf(null) }
  LaunchedEffect(Unit) {
    viewModel.toastMessageResId.collectLatest { messageResId ->
      Toast.makeText(context, messageResId, Toast.LENGTH_LONG).also {
        it.show()
        currentToast?.cancel()
        currentToast = it
      }
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun WidgetLocationScreen(
  locations: Loadable<List<StableValue<Location>>>,
  selectedLocationId: Long?,
  onLocationSelected: (Long) -> Unit,
  mode: WidgetLocationMode,
  onConfirmLocationSelectionClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var zoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

    val bottomButtonsPaddingDp = 20.dp
    var addWidgetButtonHeightPx by remember { mutableStateOf(0) }
    var zoomButtonsRowHeightPx by remember { mutableStateOf(0) }

    @Composable
    fun TopAppBar() {
      DrawerMenuTopAppBar(
        title = stringResource(commonR.string.select_widget_location),
        onDrawerMenuClick = onDrawerMenuClick
      )
    }

    AnimatedContent(
      targetState = locations,
      transitionSpec = { fadeIn() with fadeOut() },
      modifier = Modifier.fillMaxSize()
    ) { locations ->
      when (locations) {
        is Ready -> {
          Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar()

            Box(modifier = Modifier.weight(1f)) {
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
                    onSelected = onLocationSelected,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(5.dp)
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

              androidx.compose.animation.AnimatedVisibility(
                visible = selectedLocationId != null,
                enter = fadeIn(),
                exit = fadeOut(),
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
            }
          }
        }
        is Loading -> {
          Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            TopAppBar()
          }
        }
        else -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.5f))

            NoLocationsCard(
              modifier = Modifier.align(Alignment.Center).wrapContentSize().padding(20.dp),
              onAddLocationClick = onAddLocationClick
            )

            TopAppBar()
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MapCard(
  location: StableValue<Location>,
  zoom: Double,
  isSelected: Boolean,
  onSelected: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = modifier,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      DisabledMapView(
        latitude = location.value.latitude,
        longitude = location.value.longitude,
        zoom = zoom,
        modifier = Modifier.fillMaxSize()
      )

      Box(
        modifier =
          Modifier.fillMaxSize()
            .clickable(enabled = !isSelected) { onSelected(location.value.id) }
            .background(color = Color.Transparent)
      )

      LocationNameGradientOverlay()

      MarkerIcon(modifier = Modifier.align(Alignment.Center).size(36.dp))

      LocationNameLabel(
        name = location.value.name,
        modifier =
          Modifier.fillMaxWidth().align(Alignment.BottomCenter).basicMarquee().padding(5.dp)
      )

      Checkbox(
        checked = isSelected,
        onCheckedChange = { if (it) onSelected(location.value.id) },
        modifier = Modifier.align(Alignment.TopEnd)
      )
    }
  }
}

@Composable
private fun NoLocationsCard(modifier: Modifier = Modifier, onAddLocationClick: () -> Unit) {
  InfoButtonCard(
    infoText = stringResource(commonR.string.no_saved_locations_add_one),
    actionText = stringResource(commonR.string.add_location),
    onButtonClick = onAddLocationClick,
    modifier = modifier
  )
}
