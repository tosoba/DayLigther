package com.trm.daylighter.core.common.util.ext

import android.annotation.SuppressLint
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

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

  const val INITIAL_LOCATION_ZOOM = 12.0
  const val MAX_ZOOM = 20.0
  const val MIN_ZOOM = 2.0
  const val LATITUDE = 0.0
  const val LONGITUDE = 0.0
  const val ORIENTATION = 0f
}

@SuppressLint("ClickableViewAccessibility")
fun MapView.setDefaultDisabledConfig(darkMode: Boolean) {
  setTileSource(MapDefaults.tileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(false)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
  val tileSystem = MapView.getTileSystem()
  setScrollableAreaLimitLatitude(tileSystem.maxLatitude, tileSystem.minLatitude, 0)
  setScrollableAreaLimitLongitude(tileSystem.minLongitude, tileSystem.maxLongitude, 0)
  isFlingEnabled = false
  setOnTouchListener { _, _ -> true }
  if (darkMode) overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
}

fun MapView.setPosition(latitude: Double, longitude: Double, zoom: Double) {
  controller.setZoom(zoom)
  mapOrientation = MapDefaults.ORIENTATION
  setExpectedCenter(GeoPoint(latitude, longitude))
}
