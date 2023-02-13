package com.trm.daylighter.feature.locations

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.MapDefaults
import com.trm.daylighter.core.common.util.ext.setDefaultDisabledConfig
import com.trm.daylighter.core.common.util.ext.setPosition
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.composable.ZoomInButton
import com.trm.daylighter.core.ui.composable.ZoomOutButton
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.core.ui.model.StableValue

const val locationsGraphRoute = "locations_graph"
private const val locationsRoute = "locations_route"

fun NavGraphBuilder.locationsGraph(
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  nestedRoutes: NavGraphBuilder.() -> Unit
) {
  navigation(startDestination = locationsRoute, route = locationsGraphRoute) {
    composable(locationsRoute) {
      LocationsRoute(
        modifier = Modifier.fillMaxSize(),
        onEditLocationClick = onEditLocationClick,
        onAddLocationClick = onAddLocationClick,
      )
    }
    nestedRoutes()
  }
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
private fun LocationsRoute(
  modifier: Modifier = Modifier,
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  viewModel: LocationsViewModel = hiltViewModel(),
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = Empty)
  LocationsScreen(
    modifier = modifier,
    locations = locations.value,
    onSetDefaultLocationClick = viewModel::setDefaultLocation,
    onEditLocationClick = onEditLocationClick,
    onDeleteLocationClick = viewModel::deleteLocation,
    onAddLocationClick = onAddLocationClick
  )
}

@Composable
private fun LocationsScreen(
    locations: Loadable<List<StableValue<Location>>>,
    onAddLocationClick: () -> Unit,
    onSetDefaultLocationClick: (Long) -> Unit,
    onEditLocationClick: (Long) -> Unit,
    onDeleteLocationClick: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var zoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }
    var locationBeingDeleted: Location? by rememberSaveable { mutableStateOf(null) }

    when (locations) {
      is WithData -> {
        if (locations.data.isNotEmpty()) {
          LazyVerticalGrid(
            contentPadding = PaddingValues(10.dp),
            columns =
              GridCells.Fixed(
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) 2
                else 4
              )
          ) {
            items(locations.data, key = { it.value.id }) { location ->
              MapCard(
                location = location,
                zoom = zoom,
                onSetDefaultLocationClick = onSetDefaultLocationClick,
                onEditLocationClick = onEditLocationClick,
                onDeleteLocationClick = { locationBeingDeleted = it },
              )
            }
          }

          Row(modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)) {
            ZoomInButton(mapZoom = zoom, onClick = { if (zoom < MapDefaults.MAX_ZOOM) ++zoom })
            Spacer(modifier = Modifier.width(5.dp))
            ZoomOutButton(mapZoom = zoom, onClick = { if (zoom > MapDefaults.MIN_ZOOM) --zoom })
          }
        } else {
          Text(
            text = stringResource(R.string.no_locations),
            modifier = Modifier.align(Alignment.Center)
          )
        }

        FloatingActionButton(
          modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
          onClick = onAddLocationClick
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.add_a_location)
          )
        }
      }
      is WithoutData -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }

    DeleteLocationConfirmationDialog(
      locationBeingDeleted = locationBeingDeleted,
      onConfirmClick = {
        onDeleteLocationClick(requireNotNull(locationBeingDeleted))
        locationBeingDeleted = null
      },
      onDismissRequest = { locationBeingDeleted = null },
      modifier = Modifier.align(Alignment.Center)
    )
  }
}

@Composable
private fun DeleteLocationConfirmationDialog(
    locationBeingDeleted: Location?,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
  AnimatedVisibility(visible = locationBeingDeleted != null, modifier = modifier) {
    AlertDialog(
      onDismissRequest = onDismissRequest,
      confirmButton = {
        TextButton(onClick = onConfirmClick) {
          Text(text = stringResource(id = android.R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismissRequest) {
          Text(text = stringResource(id = android.R.string.cancel))
        }
      },
      title = { Text(text = stringResource(R.string.delete_location)) },
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
) {
  Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(5.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      MapView(latitude = location.value.latitude, longitude = location.value.longitude, zoom = zoom)
      Icon(
        painter = painterResource(id = commonR.drawable.marker),
        contentDescription = stringResource(id = commonR.string.location_marker),
        modifier = Modifier.align(Alignment.Center).size(36.dp)
      )
      LocationDropDrownMenu(
        modifier = Modifier.align(Alignment.BottomEnd),
        location = location,
        onSetDefaultLocationClick = onSetDefaultLocationClick,
        onEditLocationClick = onEditLocationClick,
        onDeleteLocationClick = onDeleteLocationClick,
      )
    }
  }
}

@Composable
private fun MapView(latitude: Double, longitude: Double, zoom: Double) {
  val mapView = rememberMapViewWithLifecycle()
  AndroidView(
    factory = { mapView },
    update = {
      it.setDefaultDisabledConfig()
      it.setPosition(latitude = latitude, longitude = longitude, zoom = zoom)
    }
  )
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
        contentDescription = stringResource(id = R.string.location_actions)
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
                contentDescription = stringResource(id = R.string.location_is_default)
              )
            }
          }
        },
        onClick = { if (!location.value.isDefault) onSetDefaultLocationClick(location.value.id) }
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
