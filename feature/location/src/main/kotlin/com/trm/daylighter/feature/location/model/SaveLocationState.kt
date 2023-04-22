package com.trm.daylighter.feature.location.model

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.trm.daylighter.core.common.util.MapDefaults

@Composable
internal fun rememberSaveLocationState(
  latitude: Double = MapDefaults.LATITUDE,
  longitude: Double = MapDefaults.LONGITUDE,
): SaveLocationState =
  rememberSaveable(latitude, longitude, saver = SaveLocationState.Saver) {
    SaveLocationState(latitude = latitude, longitude = longitude)
  }

@Stable
internal class SaveLocationState(
  latitude: Double = MapDefaults.LATITUDE,
  longitude: Double = MapDefaults.LONGITUDE,
) {
  var latitude by mutableStateOf(latitude)
  var longitude by mutableStateOf(longitude)

  companion object {
    val Saver: Saver<SaveLocationState, *> =
      listSaver(
        save = { listOf(it.latitude, it.longitude) },
        restore = { SaveLocationState(it[0], it[1]) }
      )
  }
}
