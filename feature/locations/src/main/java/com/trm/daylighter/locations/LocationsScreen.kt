package com.trm.daylighter.locations

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.core.common.util.map.MapDefaults
import com.trm.daylighter.domain.model.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

const val locationsRoute = "locations_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LocationsRoute(
  modifier: Modifier = Modifier,
  onAddLocationClick: () -> Unit,
  viewModel: LocationsViewModel = hiltViewModel(),
) {
  val locations = viewModel.locations.collectAsStateWithLifecycle(initialValue = Empty)
  LocationsScreen(
    modifier = modifier,
    locations = locations.value,
    onAddLocationClick = onAddLocationClick,
    onSetDefaultLocationClick = viewModel::setDefaultLocation
  )
}

@Composable
private fun LocationsScreen(
  locations: Loadable<List<Location>>,
  onAddLocationClick: () -> Unit,
  onSetDefaultLocationClick: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    var zoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }
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
            items(locations.data, key = Location::id) { location ->
              Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(5.dp),
              ) {
                Box(modifier = Modifier.fillMaxSize()) {
                  val mapView = rememberMapViewWithLifecycle()
                  AndroidView(
                    factory = { mapView },
                    update = {
                      it.setDefaultConfig()
                      it.setLocation(location = location, zoom = zoom)
                    }
                  )
                  LocationDropDrownMenu(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    location = location,
                    onSetDefaultLocationClick = onSetDefaultLocationClick
                  )
                }
              }
            }
          }
        } else {
          Text(text = "No locations", modifier = Modifier.align(Alignment.Center))
        }

        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)) {
          AnimatedVisibility(
            visible = zoom < MapDefaults.MAX_ZOOM,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            SmallFloatingActionButton(onClick = { if (zoom < MapDefaults.MAX_ZOOM) ++zoom }) {
              Icon(imageVector = Icons.Filled.ZoomIn, contentDescription = "zoom_in")
            }
          }
          Spacer(modifier = Modifier.width(5.dp))
          AnimatedVisibility(
            visible = zoom > MapDefaults.MIN_ZOOM,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            SmallFloatingActionButton(onClick = { if (zoom > MapDefaults.MIN_ZOOM) --zoom }) {
              Icon(imageVector = Icons.Filled.ZoomOut, contentDescription = "zoom_out")
            }
          }
        }

        FloatingActionButton(
          modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
          onClick = onAddLocationClick
        ) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = "add_location")
        }
      }
      is WithoutData -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  }
}

@Composable
private fun LocationDropDrownMenu(
  modifier: Modifier = Modifier,
  location: Location,
  onSetDefaultLocationClick: (Long) -> Unit
) {
  Box(modifier = modifier) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }, modifier = Modifier.align(Alignment.BottomEnd)) {
      Icon(imageVector = Icons.Default.MoreVert, contentDescription = "location_menu")
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.align(Alignment.BottomEnd)
    ) {
      DropdownMenuItem(
        text = {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Default")
            if (location.isDefault) {
              Icon(imageVector = Icons.Filled.Done, contentDescription = "location_default")
            }
          }
        },
        onClick = { if (!location.isDefault) onSetDefaultLocationClick(location.id) }
      )
      DropdownMenuItem(text = { Text(text = "Edit") }, onClick = {})
      DropdownMenuItem(text = { Text(text = "Delete") }, onClick = {})
    }
  }
}

@SuppressLint("ClickableViewAccessibility")
private fun MapView.setDefaultConfig() {
  setTileSource(MapDefaults.tileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(false)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
  val tileSystem = MapView.getTileSystem()
  setScrollableAreaLimitLatitude(tileSystem.maxLatitude, tileSystem.minLatitude, 0)
  setScrollableAreaLimitLongitude(tileSystem.minLongitude, tileSystem.maxLongitude, 0)
  isFlingEnabled = false
  setOnTouchListener { _, _ -> true }
}

private fun MapView.setLocation(location: Location, zoom: Double) {
  controller.setZoom(zoom)
  mapOrientation = MapDefaults.ORIENTATION
  setExpectedCenter(GeoPoint(location.latitude, location.longitude))
}
