package com.trm.daylighter.widget

import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeByIdUseCase
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.widget.location.daynight.DayNightCycleWidgetPinnedReceiver
import com.trm.daylighter.widget.location.daynight.DayNightCycleWidgetPreview
import com.trm.daylighter.widget.location.daynight.DayNightCycleWidgetReceiver
import com.trm.daylighter.widget.location.goldenblue.GoldenBlueHourWidgetPinnedReceiver
import com.trm.daylighter.widget.location.goldenblue.GoldenBlueHourWidgetPreview
import com.trm.daylighter.widget.location.goldenblue.GoldenBlueHourWidgetReceiver
import com.trm.daylighter.widget.util.ext.updateAllWidgetsIntent
import com.trm.daylighter.widget.util.ext.updateWidgetIntent
import com.trm.daylighter.widget.util.ext.widgetPinSuccessCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetManagerImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
  private val getLocationSunriseSunsetChangeByIdUseCase: GetLocationSunriseSunsetChangeByIdUseCase,
) : WidgetManager {
  override fun updateAllLocationWidgets() {
    context.sendBroadcast(context.updateAllWidgetsIntent<DayNightCycleWidgetReceiver>())
    context.sendBroadcast(context.updateAllWidgetsIntent<GoldenBlueHourWidgetReceiver>())
  }

  override suspend fun addDayNightCycleWidget(locationId: Long): Boolean =
    GlanceAppWidgetManager(context)
      .requestPinGlanceAppWidget(
        receiver = DayNightCycleWidgetReceiver::class.java,
        preview =
          if (Build.VERSION.SDK_INT >= 31) {
            DayNightCycleWidgetPreview(
              change = getLocationSunriseSunsetChangeByIdUseCase(locationId)
            )
          } else {
            null
          },
        previewState = null,
        successCallback =
          context.widgetPinSuccessCallback<DayNightCycleWidgetPinnedReceiver>(locationId),
      )

  override fun editDayNightCycleWidget(widgetId: Int, locationId: Long) {
    context.sendBroadcast(
      context.updateWidgetIntent<DayNightCycleWidgetReceiver>(
        widgetId = widgetId,
        locationId = locationId,
      )
    )
  }

  override suspend fun addGoldenBlueHourWidget(locationId: Long): Boolean =
    GlanceAppWidgetManager(context)
      .requestPinGlanceAppWidget(
        receiver = GoldenBlueHourWidgetReceiver::class.java,
        preview =
          if (Build.VERSION.SDK_INT >= 31) {
            GoldenBlueHourWidgetPreview(
              change = getLocationSunriseSunsetChangeByIdUseCase(locationId)
            )
          } else {
            null
          },
        previewState = null,
        successCallback =
          context.widgetPinSuccessCallback<GoldenBlueHourWidgetPinnedReceiver>(locationId),
      )

  override fun editGoldenBlueHourWidget(widgetId: Int, locationId: Long) {
    context.sendBroadcast(
      context.updateWidgetIntent<GoldenBlueHourWidgetReceiver>(
        widgetId = widgetId,
        locationId = locationId,
      )
    )
  }
}
