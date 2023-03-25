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
  name: String = "",
  nameError: LocationNameError = LocationNameError.NO_ERROR
): SaveLocationState =
  rememberSaveable(latitude, longitude, name, nameError, saver = SaveLocationState.Saver) {
    SaveLocationState(
      latitude = latitude,
      longitude = longitude,
      name = name,
      nameError = nameError
    )
  }

@Stable
internal class SaveLocationState(
  latitude: Double = MapDefaults.LATITUDE,
  longitude: Double = MapDefaults.LONGITUDE,
  name: String = "",
  nameError: LocationNameError = LocationNameError.NO_ERROR
) {
  var latitude by mutableStateOf(latitude)
  var longitude by mutableStateOf(longitude)
  var name by mutableStateOf(name)
  var nameError by mutableStateOf(nameError)

  companion object {
    val Saver: Saver<SaveLocationState, *> =
      listSaver(
        save = { listOf(it.latitude, it.longitude, it.name, it.nameError) },
        restore = {
          SaveLocationState(
            it[0] as Double,
            it[1] as Double,
            it[2] as String,
            it[3] as LocationNameError
          )
        }
      )
  }
}
