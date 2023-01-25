package com.trm.daylighter.feature.location

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.trm.daylighter.feature.location.model.MapPosition
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import org.osmdroid.views.MapView

@Composable
fun LocationScreen(modifier: Modifier = Modifier) {
  var mapPosition by rememberSaveable { mutableStateOf(MapPosition()) }

  val mapView =
    rememberMapViewWithLifecycle(
      onPause = {
        mapPosition =
          MapPosition(
            lat = it.mapCenter.latitude,
            lng = it.mapCenter.longitude,
            zoom = it.zoomLevelDouble,
            orientation = it.mapOrientation
          )
      }
    )

  Box(modifier = modifier) {
    AndroidView(
      factory = { mapView },
      update = {
        it.setDefaultConfig()
        it.restorePosition(mapPosition)
      },
      modifier = Modifier.fillMaxSize(),
    )

    Icon(
      painter = painterResource(id = R.drawable.marker),
      contentDescription = "center_marker",
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
      FloatingActionButton(onClick = {}) {
        Icon(imageVector = Icons.Filled.Done, contentDescription = "save_location")
      }
    }

    var infoExpanded by rememberSaveable { mutableStateOf(false) }
    val infoContainerColor =
      animateColorAsState(
        targetValue =
          if (infoExpanded) MaterialTheme.colorScheme.background
          else FloatingActionButtonDefaults.containerColor
      )
    FloatingActionButton(
      modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
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

@Composable
private fun rememberMapViewWithLifecycle(onPause: (MapView) -> Unit): MapView {
  val context = LocalContext.current
  val mapView = remember { MapView(context).apply { setDestroyMode(false) } }

  val mapLifecycleObserver = rememberMapLifecycleObserver(mapView, onPause)
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  DisposableEffect(lifecycle) {
    lifecycle.addObserver(mapLifecycleObserver)
    onDispose { lifecycle.removeObserver(mapLifecycleObserver) }
  }

  return mapView
}

@Composable
private fun rememberMapLifecycleObserver(
  mapView: MapView,
  onPause: (MapView) -> Unit
): LifecycleEventObserver {
  val onPauseHandler = rememberUpdatedState(newValue = onPause)
  return remember(mapView) {
    LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {
          mapView.onResume()
        }
        Lifecycle.Event.ON_PAUSE -> {
          onPauseHandler.value(mapView)
          mapView.onPause()
        }
        Lifecycle.Event.ON_DESTROY -> {
          mapView.onDetach()
        }
        else -> {}
      }
    }
  }
}
