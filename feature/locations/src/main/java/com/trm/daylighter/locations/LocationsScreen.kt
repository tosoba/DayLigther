package com.trm.daylighter.locations

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
              val mapView = rememberMapViewWithLifecycle()
              Card(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                Box(
                  modifier =
                    Modifier.fillMaxWidth()
                      .background(MaterialTheme.colorScheme.background)
                      .clip(RoundedCornerShape(12.dp))
                      .aspectRatio(1f)
                ) {
                  AndroidView(
                    factory = { mapView },
                    update = {
                      it.setDefaultConfig()
                      it.setLocation(location)
                    }
                  )

                  Checkbox(
                    checked = location.isDefault,
                    onCheckedChange = { checked ->
                      if (checked) onSetDefaultLocationClick(location.id)
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                  )
                }
                Row(
                  modifier =
                    Modifier.fillMaxWidth()
                      .background(MaterialTheme.colorScheme.background)
                      .padding(5.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  IconButton(onClick = {}) {
                    Icon(Icons.Filled.Edit, contentDescription = "edit_location")
                  }
                  IconButton(onClick = {}) {
                    Icon(Icons.Filled.Delete, contentDescription = "delete_location")
                  }
                }
              }
            }
          }
        } else {
          Text(text = "No locations", modifier = Modifier.align(Alignment.Center))
        }

        FloatingActionButton(
          onClick = onAddLocationClick,
          modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = "add_location")
        }
      }
      is WithoutData -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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

private fun MapView.setLocation(location: Location) {
  controller.setZoom(13.0)
  mapOrientation = MapDefaults.ORIENTATION
  setExpectedCenter(GeoPoint(location.latitude, location.longitude))
}
