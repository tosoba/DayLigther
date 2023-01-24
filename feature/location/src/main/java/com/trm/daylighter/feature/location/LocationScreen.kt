package com.trm.daylighter.feature.location

import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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

  AndroidView(
    factory = { mapView },
    update = {
      with(it) {
        setTileSource(defaultTileSource)
        isTilesScaledToDpi = true
        controller.setZoom(zoom)
        mapOrientation = orientation
        setMultiTouchControls(true)
        setExpectedCenter(GeoPoint(latitude, longitude))
      }
    },
    modifier = modifier,
  )
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
