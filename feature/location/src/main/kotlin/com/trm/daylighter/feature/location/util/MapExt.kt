package com.trm.daylighter.feature.location.util

import com.trm.daylighter.core.common.util.ext.MapDefaults
import com.trm.daylighter.feature.location.model.MapPosition
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

internal fun MapView.setDefaultConfig() {
  setTileSource(MapDefaults.tileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(true)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
  val tileSystem = MapView.getTileSystem()
  setScrollableAreaLimitLatitude(tileSystem.maxLatitude, tileSystem.minLatitude, 0)
  setScrollableAreaLimitLongitude(tileSystem.minLongitude, tileSystem.maxLongitude, 0)
  minZoomLevel = MapDefaults.MIN_ZOOM
}

internal fun MapView.restorePosition(position: MapPosition) {
  controller.setZoom(position.zoom)
  mapOrientation = position.orientation
  setExpectedCenter(GeoPoint(position.latitude, position.longitude))
}
