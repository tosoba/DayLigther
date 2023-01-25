package com.trm.daylighter.feature.location.util

import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource

internal object MapDefaults {
  val tileSource: XYTileSource
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

  const val MIN_ZOOM: Double = 2.0
  const val LAT = 0.0
  const val LNG = 0.0
  const val ORIENTATION = 0f
}