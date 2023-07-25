package com.trm.daylighter.core.common.util.ext

import android.annotation.SuppressLint
import android.graphics.Color
import com.trm.daylighter.core.common.model.MapDefaults
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.TilesOverlay

@SuppressLint("ClickableViewAccessibility")
fun MapView.setDefaultDisabledConfig(darkMode: Boolean) {
  setTileSource(MapDefaults.tileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(false)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
  isFlingEnabled = false
  setOnTouchListener { _, _ -> true }
  if (darkMode) overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
  addCopyrightOverlay(darkMode)
}

fun MapView.setPosition(latitude: Double, longitude: Double, zoom: Double) {
  controller.setZoom(zoom)
  mapOrientation = MapDefaults.ORIENTATION
  setExpectedCenter(GeoPoint(latitude, longitude))
}

fun MapView.addCopyrightOverlay(darkMode: Boolean) {
  overlays.add(
    CopyrightOverlay(context).apply {
      setTextColor(if (darkMode) Color.WHITE else Color.BLACK)
      setCopyrightNotice(tileProvider.tileSource.copyrightNotice)
    }
  )
}
