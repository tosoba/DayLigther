package com.trm.daylighter.widget.location

import java.util.UUID

sealed interface LocationWidgetState {
  val uuid: String
  data class DefaultLocation(override val uuid: String) : LocationWidgetState
  data class ChosenLocation(val locationId: Long, override val uuid: String) : LocationWidgetState

  fun copyWithNewUuid(): LocationWidgetState =
    when (this) {
      is ChosenLocation -> copy(uuid = UUID.randomUUID().toString())
      is DefaultLocation -> copy(uuid = UUID.randomUUID().toString())
    }
}
