package com.trm.daylighter.widget.location

import java.util.UUID
import kotlinx.serialization.Serializable

sealed interface LocationWidgetState {
  val uuid: String

  @Serializable data class DefaultLocation(override val uuid: String) : LocationWidgetState

  @Serializable
  data class ChosenLocation(val locationId: Long, override val uuid: String) : LocationWidgetState

  fun copyWithNewUuid(): LocationWidgetState =
    when (this) {
      is ChosenLocation -> copy(uuid = UUID.randomUUID().toString())
      is DefaultLocation -> copy(uuid = UUID.randomUUID().toString())
    }
}
