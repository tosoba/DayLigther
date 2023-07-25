package com.trm.daylighter.feature.location.util

import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.common.util.ext.addCopyrightOverlay
import com.trm.daylighter.feature.location.model.MapPosition
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

internal fun MapView.setDefaultConfig(darkMode: Boolean) {
  setTileSource(MapDefaults.tileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(true)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
  minZoomLevel = MapDefaults.MIN_ZOOM
  if (darkMode) overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
  addCopyrightOverlay(darkMode)
}

internal fun MapView.restorePosition(position: MapPosition) {
  controller.setZoom(position.zoom)
  mapOrientation = position.orientation
  setExpectedCenter(GeoPoint(position.latitude, position.longitude))
}
