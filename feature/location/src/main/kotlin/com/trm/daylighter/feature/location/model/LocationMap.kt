package com.trm.daylighter.feature.location.model

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.MapView

@Composable
internal fun rememberLocationMap(
  mapPosition: MapPosition,
  infoExpanded: Boolean = true
): LocationMap {
  val locationMapState =
    rememberSaveable(mapPosition, saver = LocationMapState.Saver) {
      LocationMapState(mapPosition = mapPosition, infoExpanded = infoExpanded)
    }

  val mapView =
    rememberMapViewWithLifecycle(
      onPause = {
        locationMapState.savedMapPosition =
          MapPosition(
            latitude = it.mapCenter.latitude,
            longitude = it.mapCenter.longitude,
            zoom = it.zoomLevelDouble,
            orientation = it.mapOrientation
          )
      }
    )

  val mapListener =
    remember(locationMapState) {
      object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
        override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()
        private fun onMapInteraction(): Boolean {
          locationMapState.infoExpanded = false
          return false
        }
      }
    }

  return remember { LocationMap(state = locationMapState, view = mapView, listener = mapListener) }
}

@Stable
internal class LocationMap(
  val state: LocationMapState,
  val view: MapView,
  val listener: MapListener
)

@Stable
internal class LocationMapState(mapPosition: MapPosition, infoExpanded: Boolean = true) {
  var infoExpanded by mutableStateOf(infoExpanded)
  var savedMapPosition by mutableStateOf(mapPosition)

  fun toggleInfoExpanded() {
    infoExpanded = !infoExpanded
  }

  companion object {
    val Saver: Saver<LocationMapState, *> =
      listSaver(
        save = { listOf(it.savedMapPosition, it.infoExpanded) },
        restore = {
          LocationMapState(mapPosition = it[0] as MapPosition, infoExpanded = it[1] as Boolean)
        }
      )
  }
}
