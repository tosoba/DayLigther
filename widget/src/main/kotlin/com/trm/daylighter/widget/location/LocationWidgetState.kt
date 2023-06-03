package com.trm.daylighter.widget.location

sealed interface LocationWidgetState {
  object Default : LocationWidgetState
  data class NonDefault(val locationId: Long) : LocationWidgetState
}
