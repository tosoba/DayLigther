package com.trm.daylighter.feature.location.model

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import org.osmdroid.views.MapView

@Composable
internal fun rememberLocationMap(
  mapPosition: MapPosition,
  infoExpanded: Boolean = true
): LocationMap {
  val locationMapState =
    rememberSaveable(mapPosition, infoExpanded, saver = LocationMapState.Saver) {
      LocationMapState(mapPosition = mapPosition)
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

  return remember(locationMapState) { LocationMap(state = locationMapState, view = mapView) }
}

@Stable internal class LocationMap(val state: LocationMapState, val view: MapView)

@Stable
internal class LocationMapState(mapPosition: MapPosition) {
  var savedMapPosition by mutableStateOf(mapPosition)

  fun updatePosition(latitude: Double, longitude: Double) {
    savedMapPosition = savedMapPosition.copy(latitude = latitude, longitude = longitude)
  }

  companion object {
    val Saver: Saver<LocationMapState, *> =
      listSaver(
        save = { listOf(it.savedMapPosition) },
        restore = { LocationMapState(mapPosition = it[0]) }
      )
  }
}
