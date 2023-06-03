package com.trm.daylighter.core.domain.widget

interface WidgetManager {
  fun updateDefaultLocationWidgets()
  fun addLocationWidget(locationId: Long): Boolean
}
