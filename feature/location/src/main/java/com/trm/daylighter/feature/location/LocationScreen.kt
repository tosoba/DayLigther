package com.trm.daylighter.feature.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView

@Composable
fun LocationScreen(modifier: Modifier = Modifier) {
  val mapView = rememberMapViewWithLifecycle()
  AndroidView(
    factory = { mapView },
    update = {
      with(it) {
        setTileSource(defaultTileSource)
        isTilesScaledToDpi = true
        setMultiTouchControls(true)
      }
    },
    modifier = modifier,
  )
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
  val context = LocalContext.current
  val mapView = remember { MapView(context).apply { setDestroyMode(false) } }

  val mapLifecycleObserver = rememberMapLifecycleObserver(mapView)
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  DisposableEffect(lifecycle) {
    lifecycle.addObserver(mapLifecycleObserver)
    onDispose { lifecycle.removeObserver(mapLifecycleObserver) }
  }

  return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
  remember(mapView) {
    LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> mapView.onResume()
        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
        Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
        else -> {}
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
