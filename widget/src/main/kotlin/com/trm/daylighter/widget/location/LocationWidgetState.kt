package com.trm.daylighter.widget.location

sealed interface LocationWidgetState {
  object DefaultLocation : LocationWidgetState
  data class ChosenLocation(val locationId: Long) : LocationWidgetState
}
