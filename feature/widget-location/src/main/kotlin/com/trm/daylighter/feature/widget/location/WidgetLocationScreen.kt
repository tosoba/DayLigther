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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.painterResource
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
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.composable.DrawerMenuTopAppBar
import com.trm.daylighter.core.ui.composable.InfoButtonCard
import com.trm.daylighter.core.ui.composable.LocationNameGradientOverlay
import com.trm.daylighter.core.ui.composable.LocationNameLabel
import com.trm.daylighter.core.ui.composable.MarkerIcon
import com.trm.daylighter.core.ui.composable.ZoomControlsRow
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.backgroundToTransparentVerticalGradient
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
    onAddDayNightCycleWidget = viewModel::onAddDayNightCycleWidget,
    onAddGoldenBlueHourWidget = viewModel::onAddGoldenBlueHourWidget,
    onEditWidgetLocationClick = viewModel::onEditWidgetLocationClick,
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WidgetLocationScreen(
  locations: Loadable<List<StableValue<Location>>>,
  selectedLocationId: Long?,
  onLocationSelected: (Long?) -> Unit,
  mode: WidgetLocationMode,
  onAddDayNightCycleWidget: () -> Unit,
  onAddGoldenBlueHourWidget: () -> Unit,
  onEditWidgetLocationClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var zoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

    val bottomButtonsPaddingDp = 20.dp
    var addWidgetButtonHeightPx by remember { mutableStateOf(0) }
    var zoomButtonsRowHeightPx by remember { mutableStateOf(0) }
    val spacerHeightPx by
      remember(selectedLocationId) {
        derivedStateOf {
          if (selectedLocationId == null) zoomButtonsRowHeightPx else addWidgetButtonHeightPx
        }
      }

    @Composable
    fun TopAppBar() {
      DrawerMenuTopAppBar(
        title = stringResource(commonR.string.select_widget_location),
        navigationIcon = { DrawerMenuIconButton(onClick = onDrawerMenuClick) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.background(backgroundToTransparentVerticalGradient)
      )
    }

    AnimatedContent(
      targetState = locations,
      transitionSpec = { fadeIn() with fadeOut() },
      modifier = Modifier.fillMaxSize()
    ) { locations ->
      when (locations) {
        is Ready -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.25f))

            Column(modifier = Modifier.fillMaxSize()) {
              TopAppBar()

              Box(modifier = Modifier.weight(1f)) {
                val columnsCount =
                  if (
                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
                  ) {
                    2
                  } else {
                    4
                  }
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

                androidx.compose.animation.AnimatedVisibility(
                  visible = selectedLocationId == null,
                  enter = fadeIn(),
                  exit = fadeOut(),
                  modifier =
                    Modifier.align(Alignment.BottomStart)
                      .padding(bottomButtonsPaddingDp)
                      .onGloballyPositioned { zoomButtonsRowHeightPx = it.size.height }
                ) {
                  ZoomControlsRow(
                    zoom = zoom,
                    incrementZoom = { ++zoom },
                    decrementZoom = { --zoom },
                  )
                }

                androidx.compose.animation.AnimatedVisibility(
                  visible = selectedLocationId != null,
                  enter = fadeIn(),
                  exit = fadeOut(),
                  modifier =
                    Modifier.align(Alignment.BottomEnd)
                      .padding(bottomButtonsPaddingDp)
                      .onGloballyPositioned { addWidgetButtonHeightPx = it.size.height }
                ) {
                  ConfirmLocationSelectionControls(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    mode = mode,
                    onConfirmDayNightCycleLocationSelectionClick = onAddDayNightCycleWidget,
                    onConfirmGoldenBlueHourLocationSelectionClick = onAddGoldenBlueHourWidget,
                    onEditWidgetLocationClick = onEditWidgetLocationClick,
                  )
                }
              }
            }
          }
        }
        is Loading -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.25f))

            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )

            TopAppBar()
          }
        }
        else -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.25f))

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

@Composable
private fun ConfirmLocationSelectionControls(
  mode: WidgetLocationMode,
  onConfirmDayNightCycleLocationSelectionClick: () -> Unit,
  onConfirmGoldenBlueHourLocationSelectionClick: () -> Unit,
  onEditWidgetLocationClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  when (mode) {
    WidgetLocationMode.ADD -> {
      Column(modifier = modifier) {
        ConfirmSelectionButton(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(commonR.string.day_night_cycle),
          icon = {
            Icon(
              painter = painterResource(commonR.drawable.day_night_cycle),
              contentDescription = stringResource(commonR.string.day_night_cycle)
            )
          },
          onClick = onConfirmDayNightCycleLocationSelectionClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        ConfirmSelectionButton(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(commonR.string.golden_blue_hour),
          icon = {
            Icon(
              painter = painterResource(commonR.drawable.day_night_cycle),
              contentDescription = stringResource(commonR.string.golden_blue_hour)
            )
          },
          onClick = onConfirmGoldenBlueHourLocationSelectionClick
        )
      }
    }
    WidgetLocationMode.EDIT -> {
      ConfirmSelectionButton(
        modifier = modifier,
        text = stringResource(commonR.string.confirm),
        icon = {
          Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = stringResource(commonR.string.confirm)
          )
        },
        onClick = onEditWidgetLocationClick
      )
    }
  }
}

@Composable
private fun ConfirmSelectionButton(
  text: String,
  icon: @Composable () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  ExtendedFloatingActionButton(
    modifier = modifier,
    text = {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
      )
    },
    icon = icon,
    onClick = onClick
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MapCard(
  location: StableValue<Location>,
  zoom: Double,
  isSelected: Boolean,
  onSelected: (Long?) -> Unit,
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
            .clickable { onSelected(if (isSelected) null else location.value.id) }
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
        onCheckedChange = { isChecked -> onSelected(if (isChecked) location.value.id else null) },
        modifier = Modifier.align(Alignment.TopEnd)
      )
    }
  }
}

@Composable
private fun NoLocationsCard(modifier: Modifier = Modifier, onAddLocationClick: () -> Unit) {
  InfoButtonCard(
    infoText = stringResource(commonR.string.no_saved_locations),
    actionText = stringResource(commonR.string.add_location),
    onButtonClick = onAddLocationClick,
    modifier = modifier
  )
}
