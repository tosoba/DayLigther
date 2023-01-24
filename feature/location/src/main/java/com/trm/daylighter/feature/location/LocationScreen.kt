package com.trm.daylighter.feature.location

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView

@Composable
fun LocationScreen(modifier: Modifier = Modifier) {
  AndroidView(
    factory = { MapView(it).apply { setDestroyMode(false) } },
    update = { mapView ->
      with(mapView) {
        setTileSource(
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
        )
        isTilesScaledToDpi = true
        setMultiTouchControls(true)
      }
    },
    modifier = modifier,
  )
}
