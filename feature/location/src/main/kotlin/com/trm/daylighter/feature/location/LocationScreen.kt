package com.trm.daylighter.feature.location

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.feature.location.model.MapPosition
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent

const val locationRoute = "location_route"

const val locationIdParam = "locationId"
const val editLocationRoute = "location_route/{${locationIdParam}}"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LocationRoute(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LocationViewModel = hiltViewModel()
) {
  LaunchedEffect(Unit) { viewModel.savedFlow.collect { onBackClick() } }
  val mapPosition = viewModel.initialMapPositionFlow.collectAsStateWithLifecycle()
  LocationScreen(
    mapPosition = mapPosition.value,
    onSaveLocationClick = viewModel::saveLocation,
    modifier = modifier,
    onBackClick = onBackClick
  )
}

@Composable
private fun LocationScreen(
  mapPosition: MapPosition,
  onSaveLocationClick: (lat: Double, lng: Double) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var savedMapPosition by rememberSaveable(mapPosition) { mutableStateOf(mapPosition) }
  var currentMapPosition by remember { mutableStateOf(savedMapPosition) }
  var infoExpanded by rememberSaveable { mutableStateOf(true) }

  val mapView =
    rememberMapViewWithLifecycle(
      onPause = {
        savedMapPosition =
          MapPosition(
            latitude = it.mapCenter.latitude,
            longitude = it.mapCenter.longitude,
            zoom = it.zoomLevelDouble,
            orientation = it.mapOrientation
          )
      }
    )
  val mapListener = remember {
    object : MapListener {
      override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
      override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()
      private fun onMapInteraction(): Boolean {
        infoExpanded = false
        val mapCenter = mapView.mapCenter
        currentMapPosition =
          MapPosition(
            latitude = mapCenter.latitude,
            longitude = mapCenter.longitude,
            zoom = mapView.zoomLevelDouble,
            orientation = mapView.mapOrientation
          )
        return false
      }
    }
  }

  Box(modifier = modifier) {
    AndroidView(
      factory = { mapView },
      update = {
        it.setDefaultConfig()
        it.removeMapListener(mapListener)
        it.restorePosition(savedMapPosition)
        it.addMapListener(mapListener)
      },
      modifier = Modifier.fillMaxSize(),
    )

    Icon(
      painter = painterResource(id = commonR.drawable.marker),
      contentDescription = stringResource(id = commonR.string.location_marker),
      modifier = Modifier.align(Alignment.Center)
    )

    Column(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
      FloatingActionButton(onClick = {}) {
        Icon(
          imageVector = Icons.Filled.MyLocation,
          contentDescription = "my_location",
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      FloatingActionButton(
        onClick = { onSaveLocationClick(currentMapPosition.latitude, currentMapPosition.longitude) }
      ) {
        Icon(imageVector = Icons.Filled.Done, contentDescription = "save_location")
      }
    }

    Row(modifier = Modifier.padding(20.dp)) {
      SmallFloatingActionButton(onClick = onBackClick, modifier = Modifier.padding(end = 5.dp)) {
        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
      }

      Spacer(modifier = Modifier.weight(1f))

      val infoContainerColor =
        animateColorAsState(
          targetValue =
            if (infoExpanded) MaterialTheme.colorScheme.background
            else FloatingActionButtonDefaults.containerColor
        )
      FloatingActionButton(
        modifier = Modifier.padding(start = 5.dp),
        containerColor = infoContainerColor.value,
        onClick = { infoExpanded = !infoExpanded }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(imageVector = Icons.Filled.Info, contentDescription = "location_info")
          AnimatedVisibility(visible = infoExpanded) {
            Row {
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = stringResource(R.string.center_map_on_location),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}
