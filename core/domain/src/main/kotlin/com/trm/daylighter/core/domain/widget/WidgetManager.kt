package com.trm.daylighter.core.domain.widget

interface WidgetManager {
  fun updateAllLocationWidgets()

  suspend fun addDayNightCycleWidget(locationId: Long): Boolean

  suspend fun editDayNightCycleWidget(widgetId: Int, locationId: Long)

  suspend fun addGoldenBlueHourWidget(locationId: Long): Boolean

  suspend fun editGoldenBlueHourWidget(widgetId: Int, locationId: Long)
}
