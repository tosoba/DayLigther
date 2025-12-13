package com.trm.daylighter.feature.locations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.trm.daylighter.core.ui.composable.ZoomButtons
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.backgroundToTransparentVerticalGradient
import com.trm.daylighter.core.ui.util.ext.fullWidthSpan
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer

const val locationsRoute = "locations_route"

@Composable
fun LocationsRoute(
  modifier: Modifier = Modifier,
  onNewLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onDrawerMenuClick: () -> Unit,
  viewModel: LocationsViewModel = hiltViewModel(),
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = LoadingFirst)

  LocationsScreen(
    modifier = modifier,
    locations = locations.value,
    onSetDefaultLocationClick = viewModel::setDefaultLocation,
    onEditLocationClick = onEditLocationClick,
    onDeleteLocationClick = viewModel::deleteLocation,
    onNewLocationClick = onNewLocationClick,
    onDrawerMenuClick = onDrawerMenuClick,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsScreen(
  locations: Loadable<List<StableValue<Location>>>,
  onNewLocationClick: () -> Unit,
  onSetDefaultLocationClick: (Long) -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onDeleteLocationClick: (Location) -> Unit,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    var locationBeingDeleted: Location? by rememberSaveable { mutableStateOf(null) }
    var zoom by rememberSaveable { mutableDoubleStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

    val bottomButtonsPaddingDp = 16.dp
    var bottomButtonsHeightPx by remember { mutableIntStateOf(0) }

    @Composable
    fun TopAppBar(modifier: Modifier = Modifier) {
      DayLighterTopAppBar(
        title = stringResource(commonR.string.locations),
        navigationIcon = {
          if (!usingPermanentNavigationDrawer) {
            DrawerMenuIconButton(onClick = onDrawerMenuClick)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier,
      )
    }

    Crossfade(targetState = locations, modifier = Modifier.fillMaxSize()) { locations ->
      when (locations) {
        is Ready -> {
          var topAppBarHeightPx by remember { mutableIntStateOf(0) }

          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.15f))

            LazyVerticalGrid(
              contentPadding = PaddingValues(12.dp),
              columns = GridCells.Adaptive(175.dp),
            ) {
              item(span = LazyGridItemSpanScope::fullWidthSpan) {
                Spacer(
                  modifier =
                    Modifier.height(with(LocalDensity.current) { topAppBarHeightPx.toDp() })
                )
              }

              items(locations.data, key = { it.value.id }) { location ->
                MapCard(
                  modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(4.dp),
                  location = location,
                  zoom = zoom,
                  onSetDefaultLocationClick = onSetDefaultLocationClick,
                  onEditLocationClick = onEditLocationClick,
                  onDeleteLocationClick = { locationBeingDeleted = it },
                )
              }

              item(span = LazyGridItemSpanScope::fullWidthSpan) {
                Spacer(
                  modifier =
                    Modifier.height(
                      bottomButtonsPaddingDp * 2 +
                        with(LocalDensity.current) { bottomButtonsHeightPx.toDp() }
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

            Column(
              modifier =
                Modifier.align(Alignment.BottomEnd)
                  .padding(bottomButtonsPaddingDp)
                  .onGloballyPositioned { bottomButtonsHeightPx = it.size.height },
              horizontalAlignment = Alignment.End,
            ) {
              ZoomButtons(zoom = zoom, incrementZoom = { ++zoom }, decrementZoom = { --zoom })

              Spacer(modifier = Modifier.height(16.dp))

              ExtendedFloatingActionButton(
                onClick = onNewLocationClick,
                icon = {
                  Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(commonR.string.new_location),
                  )
                },
                text = { Text(stringResource(commonR.string.new_location)) },
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
              modifier = Modifier.align(Alignment.Center).padding(16.dp),
              onNewLocationClick = onNewLocationClick,
            )

            TopAppBar(modifier = Modifier.background(backgroundToTransparentVerticalGradient))
          }
        }
      }

      DeleteLocationConfirmationDialog(
        locationBeingDeleted = locationBeingDeleted,
        onConfirmClick = {
          onDeleteLocationClick(requireNotNull(locationBeingDeleted))
          locationBeingDeleted = null
        },
        onDismissRequest = { locationBeingDeleted = null },
        modifier = Modifier.align(Alignment.Center).wrapContentHeight(),
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
    modifier = modifier,
  )
}

@Composable
private fun DeleteLocationConfirmationDialog(
  locationBeingDeleted: Location?,
  onConfirmClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = locationBeingDeleted != null,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier,
  ) {
    AlertDialog(
      onDismissRequest = onDismissRequest,
      confirmButton = {
        TextButton(onClick = onConfirmClick) { Text(text = stringResource(android.R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = onDismissRequest) {
          Text(text = stringResource(android.R.string.cancel))
        }
      },
      title = {
        Text(text = stringResource(R.string.delete_location), textAlign = TextAlign.Center)
      },
      text = { Text(text = stringResource(R.string.delete_location_prompt)) },
    )
  }
}

@Composable
private fun MapCard(
  location: StableValue<Location>,
  zoom: Double,
  onSetDefaultLocationClick: (Long) -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onDeleteLocationClick: (Location) -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
      DisabledMapView(
        latitude = location.value.latitude,
        longitude = location.value.longitude,
        zoom = zoom,
      )

      LocationNameGradientOverlay()

      MarkerIcon(modifier = Modifier.align(Alignment.Center).size(36.dp))

      LocationNameLabel(
        name = location.value.name,
        modifier =
          Modifier.fillMaxWidth()
            .align(Alignment.BottomCenter)
            .basicMarquee(iterations = Int.MAX_VALUE)
            .padding(vertical = 12.dp, horizontal = 4.dp),
      )

      LocationDropDrownMenu(
        modifier = Modifier.align(Alignment.TopEnd),
        location = location,
        onSetDefaultLocationClick = onSetDefaultLocationClick,
        onEditLocationClick = onEditLocationClick,
        onDeleteLocationClick = onDeleteLocationClick,
      )
    }
  }
}

@Composable
private fun LocationDropDrownMenu(
  modifier: Modifier = Modifier,
  location: StableValue<Location>,
  onSetDefaultLocationClick: (Long) -> Unit,
  onDeleteLocationClick: (Location) -> Unit,
  onEditLocationClick: (Long) -> Unit,
) {
  Box(modifier = modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    FilledTonalIconButton(
      onClick = { expanded = true },
      modifier = Modifier.align(Alignment.BottomEnd),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = stringResource(R.string.location_actions),
      )
    }

    fun hideDropdown() {
      expanded = false
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = ::hideDropdown,
      modifier = Modifier.align(Alignment.BottomEnd),
    ) {
      DropdownMenuItem(
        text = {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(text = stringResource(R.string.set_as_default))
            if (location.value.isDefault) {
              Spacer(modifier = Modifier.width(3.dp))
              Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = stringResource(R.string.location_is_default),
              )
            }
          }
        },
        onClick = {
          if (!location.value.isDefault) {
            onSetDefaultLocationClick(location.value.id)
            hideDropdown()
          }
        },
      )
      DropdownMenuItem(
        text = { Text(text = stringResource(R.string.edit)) },
        onClick = {
          onEditLocationClick(location.value.id)
          hideDropdown()
        },
      )
      DropdownMenuItem(
        text = { Text(text = stringResource(R.string.delete)) },
        onClick = {
          onDeleteLocationClick(location.value)
          hideDropdown()
        },
      )
    }
  }
}
