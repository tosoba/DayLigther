package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.trm.daylighter.core.common.util.ext.setDefaultDisabledConfig
import com.trm.daylighter.core.common.util.ext.setPosition
import org.osmdroid.views.MapView

@Composable
fun rememberMapViewWithLifecycle(onPause: (MapView) -> Unit = {}): MapView {
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
fun rememberMapLifecycleObserver(
  mapView: MapView,
  onPause: (MapView) -> Unit,
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

@Composable
fun DisabledMapView(
  latitude: Double,
  longitude: Double,
  zoom: Double,
  modifier: Modifier = Modifier,
) {
  val mapView = rememberMapViewWithLifecycle()
  val darkMode = isSystemInDarkTheme()
  AndroidView(
    modifier = modifier,
    factory = { mapView },
    update = {
      it.setDefaultDisabledConfig(darkMode = darkMode)
      it.setPosition(latitude = latitude, longitude = longitude, zoom = zoom)
    },
  )
}
