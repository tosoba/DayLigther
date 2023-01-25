package com.trm.daylighter.feature.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
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

    FloatingActionButton(
      modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
      onClick = {},
    ) {
      Icon(
        imageVector = Icons.Filled.MyLocation,
        contentDescription = "my_location",
      )
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
