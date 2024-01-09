package com.trm.daylighter.feature.widget.location

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DayPeriodChart
import com.trm.daylighter.core.ui.composable.DisabledMapView
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.composable.InfoButtonCard
import com.trm.daylighter.core.ui.composable.LocationNameGradientOverlay
import com.trm.daylighter.core.ui.composable.LocationNameLabel
import com.trm.daylighter.core.ui.composable.MarkerIcon
import com.trm.daylighter.core.ui.composable.ZoomButtonsRow
import com.trm.daylighter.core.ui.local.LocalWidthSizeClass
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.backgroundToTransparentVerticalGradient
import com.trm.daylighter.core.ui.util.ext.fullWidthSpan
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer
import kotlinx.coroutines.flow.collectLatest

const val widgetLocationRoute = "widget_location_route"

@Composable
fun WidgetLocationRoute(
  modifier: Modifier = Modifier,
  onNewLocationClick: () -> Unit,
  onDrawerMenuClick: () -> Unit,
  backHandler: @Composable () -> Unit,
  viewModel: WidgetLocationViewModel = hiltViewModel()
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = LoadingFirst)
  val selectedLocationId = viewModel.selectedLocationIdFlow.collectAsStateWithLifecycle()

  backHandler()

  WidgetLocationScreen(
    locations = locations.value,
    selectedLocationId = selectedLocationId.value,
    onLocationSelected = { viewModel.selectedLocationId = it },
    mode = viewModel.mode,
    onAddDayNightCycleWidget = viewModel::onAddDayNightCycleWidget,
    onAddGoldenBlueHourWidget = viewModel::onAddGoldenBlueHourWidget,
    onEditWidgetLocationClick = viewModel::onEditWidgetLocationClick,
    onNewLocationClick = onNewLocationClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetLocationScreen(
  locations: Loadable<List<StableValue<Location>>>,
  selectedLocationId: Long?,
  onLocationSelected: (Long?) -> Unit,
  mode: WidgetLocationMode,
  onAddDayNightCycleWidget: () -> Unit,
  onAddGoldenBlueHourWidget: () -> Unit,
  onEditWidgetLocationClick: () -> Unit,
  onNewLocationClick: () -> Unit,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var zoom by rememberSaveable { mutableDoubleStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

    val bottomButtonsPaddingDp = 20.dp
    var addWidgetButtonsHeightPx by remember { mutableIntStateOf(0) }
    var zoomButtonsRowHeightPx by remember { mutableIntStateOf(0) }

    @Composable
    fun TopAppBar(modifier: Modifier = Modifier) {
      DayLighterTopAppBar(
        title = stringResource(commonR.string.select_widget_location),
        navigationIcon = {
          if (!usingPermanentNavigationDrawer) {
            DrawerMenuIconButton(onClick = onDrawerMenuClick)
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
      )
    }

    Crossfade(
      targetState = locations,
      modifier = Modifier.fillMaxSize(),
      label = "widget-location-chart-crossfade"
    ) { locations ->
      when (locations) {
        is Ready -> {
          var topAppBarHeightPx by remember { mutableIntStateOf(0) }

          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.15f))

            LazyVerticalGrid(
              contentPadding = PaddingValues(10.dp),
              columns = GridCells.Adaptive(175.dp)
            ) {
              item(span = LazyGridItemSpanScope::fullWidthSpan) {
                Spacer(
                  modifier =
                    Modifier.height(with(LocalDensity.current) { topAppBarHeightPx.toDp() })
                )
              }

              items(locations.data, key = { it.value.id }) { location ->
                MapCard(
                  location = location,
                  zoom = zoom,
                  isSelected = location.value.id == selectedLocationId,
                  onSelected = onLocationSelected,
                  modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(5.dp)
                )
              }

              item(span = LazyGridItemSpanScope::fullWidthSpan) {
                Spacer(
                  modifier =
                    Modifier.height(
                      bottomButtonsPaddingDp * 2 +
                        with(LocalDensity.current) {
                          if (selectedLocationId == null) {
                              zoomButtonsRowHeightPx
                            } else {
                              addWidgetButtonsHeightPx
                            }
                            .toDp()
                        }
                    )
                )
              }
            }

            TopAppBar(
              modifier =
                Modifier.background(backgroundToTransparentVerticalGradient).onGloballyPositioned {
                  topAppBarHeightPx = it.size.height
                }
            )

            androidx.compose.animation.AnimatedVisibility(
              visible = selectedLocationId == null,
              enter = fadeIn(),
              exit = fadeOut(),
              modifier =
                Modifier.align(Alignment.BottomStart)
                  .padding(bottomButtonsPaddingDp)
                  .onGloballyPositioned { zoomButtonsRowHeightPx = it.size.height }
            ) {
              ZoomButtonsRow(
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
                  .onGloballyPositioned { addWidgetButtonsHeightPx = it.size.height }
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
        is Loading -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.15f))

            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )

            TopAppBar(modifier = Modifier.background(backgroundToTransparentVerticalGradient))
          }
        }
        else -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.15f))

            NoLocationsCard(
              modifier = Modifier.align(Alignment.Center).wrapContentSize().padding(20.dp),
              onNewLocationClick = onNewLocationClick
            )

            TopAppBar(modifier = Modifier.background(backgroundToTransparentVerticalGradient))
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
      if (LocalWidthSizeClass.current == WindowWidthSizeClass.Compact) {
        Column(modifier = modifier) {
          ConfirmSelectionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.add_day_night_cycle_widget),
            icon = {
              Icon(
                painter = painterResource(commonR.drawable.day_night_cycle),
                contentDescription = stringResource(R.string.add_day_night_cycle_widget)
              )
            },
            onClick = onConfirmDayNightCycleLocationSelectionClick
          )

          Spacer(modifier = Modifier.height(5.dp))

          ConfirmSelectionButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.add_golden_blue_hour_widget),
            icon = {
              Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = stringResource(R.string.add_golden_blue_hour_widget)
              )
            },
            onClick = onConfirmGoldenBlueHourLocationSelectionClick
          )
        }
      } else {
        Row(modifier = modifier) {
          ConfirmSelectionButton(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.add_day_night_cycle_widget),
            icon = {
              Icon(
                painter = painterResource(commonR.drawable.day_night_cycle),
                contentDescription = stringResource(R.string.add_day_night_cycle_widget)
              )
            },
            onClick = onConfirmDayNightCycleLocationSelectionClick
          )

          Spacer(modifier = Modifier.width(5.dp))

          ConfirmSelectionButton(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.add_golden_blue_hour_widget),
            icon = {
              Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = stringResource(R.string.add_golden_blue_hour_widget)
              )
            },
            onClick = onConfirmGoldenBlueHourLocationSelectionClick
          )
        }
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

@OptIn(ExperimentalFoundationApi::class)
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
        modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE),
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1
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
          Modifier.fillMaxWidth()
            .align(Alignment.BottomCenter)
            .basicMarquee(iterations = Int.MAX_VALUE)
            .padding(vertical = 10.dp, horizontal = 5.dp)
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
private fun NoLocationsCard(modifier: Modifier = Modifier, onNewLocationClick: () -> Unit) {
  InfoButtonCard(
    infoText = stringResource(commonR.string.no_saved_locations),
    actionText = stringResource(commonR.string.new_location),
    onButtonClick = onNewLocationClick,
    modifier = modifier
  )
}
