package com.trm.daylighter.core.common.model

import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource

object MapDefaults {
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
          "https://c.tile.openstreetmap.org/",
        ),
        "© OpenStreetMap contributors",
        TileSourcePolicy(
          2,
          TileSourcePolicy.FLAG_NO_BULK or
            TileSourcePolicy.FLAG_NO_PREVENTIVE or
            TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or
            TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED,
        ),
      )

  const val INITIAL_LOCATION_ZOOM = 12.0
  const val MAX_ZOOM = 20.0
  const val MIN_ZOOM = 2.0
  const val LATITUDE = 0.0
  const val LONGITUDE = 0.0
  const val ORIENTATION = 0f
  const val LABEL = ""
}
