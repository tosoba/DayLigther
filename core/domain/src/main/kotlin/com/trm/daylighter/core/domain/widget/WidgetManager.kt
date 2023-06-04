package com.trm.daylighter.core.domain.widget

interface WidgetManager {
  fun updateDefaultLocationWidgets()

  suspend fun addLocationWidget(locationId: Long): Boolean

  suspend fun editLocationWidget(widgetId: Int, locationId: Long)
}
