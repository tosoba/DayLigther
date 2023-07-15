package com.trm.daylighter.feature.locations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.composable.*
import com.trm.daylighter.core.ui.ext.fullWidthSpan
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.backgroundToTransparentVerticalGradient

const val locationsRoute = "locations_route"

@Composable
fun LocationsRoute(
  modifier: Modifier = Modifier,
  onAddLocationClick: () -> Unit,
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
    onAddLocationClick = onAddLocationClick,
    onDrawerMenuClick = onDrawerMenuClick,
  )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LocationsScreen(
  locations: Loadable<List<StableValue<Location>>>,
  onAddLocationClick: () -> Unit,
  onSetDefaultLocationClick: (Long) -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onDeleteLocationClick: (Location) -> Unit,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var locationBeingDeleted: Location? by rememberSaveable { mutableStateOf(null) }
    var zoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

    val bottomButtonsPaddingDp = 20.dp
    var bottomButtonsHeightPx by remember { mutableStateOf(0) }

    @Composable
    fun TopAppBar(modifier: Modifier = Modifier) {
      DayLighterTopAppBar(
        title = stringResource(commonR.string.locations),
        navigationIcon = { DrawerMenuIconButton(onClick = onDrawerMenuClick) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
      )
    }

    AnimatedContent(
      targetState = locations,
      transitionSpec = { fadeIn() with fadeOut() },
      modifier = Modifier.fillMaxSize()
    ) { locations ->
      when (locations) {
        is Ready -> {
          var topAppBarHeightPx by remember { mutableStateOf(0) }

          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.25f))

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
                  modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(5.dp),
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

            ZoomControlsRow(
              zoom = zoom,
              incrementZoom = { ++zoom },
              decrementZoom = { --zoom },
              modifier = Modifier.align(Alignment.BottomStart).padding(bottomButtonsPaddingDp)
            )

            FloatingActionButton(
              onClick = onAddLocationClick,
              modifier =
                Modifier.align(Alignment.BottomEnd)
                  .padding(bottomButtonsPaddingDp)
                  .onGloballyPositioned { bottomButtonsHeightPx = it.size.height }
            ) {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_a_location)
              )
            }
          }
        }
        is Loading -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.25f))

            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )

            TopAppBar(modifier = Modifier.background(backgroundToTransparentVerticalGradient))
          }
        }
        else -> {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(change = Empty.asStable(), modifier = Modifier.fillMaxSize().alpha(.25f))

            NoLocationsCard(
              modifier = Modifier.align(Alignment.Center).padding(20.dp),
              onAddLocationClick = onAddLocationClick
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
        modifier = Modifier.align(Alignment.Center).wrapContentHeight()
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

@Composable
private fun DeleteLocationConfirmationDialog(
  locationBeingDeleted: Location?,
  onConfirmClick: () -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = locationBeingDeleted != null,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MapCard(
  location: StableValue<Location>,
  zoom: Double,
  onSetDefaultLocationClick: (Long) -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onDeleteLocationClick: (Location) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = modifier,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      DisabledMapView(
        latitude = location.value.latitude,
        longitude = location.value.longitude,
        zoom = zoom
      )

      LocationNameGradientOverlay()

      MarkerIcon(modifier = Modifier.align(Alignment.Center).size(36.dp))

      Row(
        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
        verticalAlignment = Alignment.CenterVertically
      ) {
        LocationNameLabel(
          name = location.value.name,
          modifier = Modifier.weight(1f).basicMarquee().padding(5.dp)
        )
        LocationDropDrownMenu(
          location = location,
          onSetDefaultLocationClick = onSetDefaultLocationClick,
          onEditLocationClick = onEditLocationClick,
          onDeleteLocationClick = onDeleteLocationClick,
        )
      }
    }
  }
}

@Composable
private fun LocationDropDrownMenu(
  location: StableValue<Location>,
  onSetDefaultLocationClick: (Long) -> Unit,
  onDeleteLocationClick: (Location) -> Unit,
  onEditLocationClick: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }, modifier = Modifier.align(Alignment.BottomEnd)) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = stringResource(R.string.location_actions)
      )
    }

    fun hideDropdown() {
      expanded = false
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = ::hideDropdown,
      modifier = Modifier.align(Alignment.BottomEnd)
    ) {
      DropdownMenuItem(
        text = {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = stringResource(R.string.set_as_default))
            if (location.value.isDefault) {
              Spacer(modifier = Modifier.width(3.dp))
              Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = stringResource(R.string.location_is_default)
              )
            }
          }
        },
        onClick = {
          if (!location.value.isDefault) {
            onSetDefaultLocationClick(location.value.id)
            hideDropdown()
          }
        }
      )
      DropdownMenuItem(
        text = { Text(text = stringResource(R.string.edit)) },
        onClick = {
          onEditLocationClick(location.value.id)
          hideDropdown()
        }
      )
      DropdownMenuItem(
        text = { Text(text = stringResource(R.string.delete)) },
        onClick = {
          onDeleteLocationClick(location.value)
          hideDropdown()
        }
      )
    }
  }
}
