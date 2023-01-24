package com.trm.daylighter.feature.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun LocationScreen(modifier: Modifier = Modifier) {
  var latitude by rememberSaveable { mutableStateOf(0.0) }
  var longitude by rememberSaveable { mutableStateOf(0.0) }
  var zoom by rememberSaveable { mutableStateOf(0.0) }
  var orientation by rememberSaveable { mutableStateOf(0f) }

  val mapView =
    rememberMapViewWithLifecycle(
      onPause = {
        latitude = it.mapCenter.latitude
        longitude = it.mapCenter.longitude
        zoom = it.zoomLevelDouble
        orientation = it.mapOrientation
      }
    )

  Box(modifier = modifier) {
    AndroidView(
      factory = { mapView },
      update = {
        with(it) {
          setDefaultConfig()
          restoreState(
            latitude = latitude,
            longitude = longitude,
            zoom = zoom,
            orientation = orientation
          )
        }
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
fun rememberMapViewWithLifecycle(onPause: (MapView) -> Unit): MapView {
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

private fun MapView.setDefaultConfig() {
  setTileSource(defaultTileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(true)
}

private fun MapView.restoreState(
  latitude: Double,
  longitude: Double,
  zoom: Double,
  orientation: Float
) {
  controller.setZoom(zoom)
  mapOrientation = orientation
  setExpectedCenter(GeoPoint(latitude, longitude))
}

private val defaultTileSource: XYTileSource
  get() =
    XYTileSource(
      "Mapnik",
      0,
      19,
      256,
      ".png",
      arrayOf(
        "https://a.tile.openstreetmap.org/",
        "https://b.tile.openstreetmap.org/",
        "https://c.tile.openstreetmap.org/"
      ),
      "© OpenStreetMap contributors",
      TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_NO_BULK or
          TileSourcePolicy.FLAG_NO_PREVENTIVE or
          TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or
          TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
      )
    )
