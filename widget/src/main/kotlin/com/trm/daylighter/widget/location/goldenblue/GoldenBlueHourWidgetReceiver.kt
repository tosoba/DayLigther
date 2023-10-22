package com.trm.daylighter.widget.location.goldenblue

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeFlowByIdUseCase
import com.trm.daylighter.widget.location.LocationWidgetActions
import com.trm.daylighter.widget.location.LocationWidgetExtras
import com.trm.daylighter.widget.location.WidgetLocationIdUpdate
import com.trm.daylighter.widget.location.updateUuid
import com.trm.daylighter.widget.util.ext.updateAllWidgets
import com.trm.daylighter.widget.util.ext.updateWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GoldenBlueHourWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject
  internal lateinit var getDefaultLocationSunriseSunsetChangeFlowUseCase:
    GetDefaultLocationSunriseSunsetChangeFlowUseCase

  @Inject
  internal lateinit var getLocationSunriseSunsetChangeFlowByIdUseCase:
    GetLocationSunriseSunsetChangeFlowByIdUseCase

  override val glanceAppWidget: GoldenBlueHourWidget
    get() =
      GoldenBlueHourWidget(
        getDefaultLocationSunriseSunsetChangeFlowUseCase,
        getLocationSunriseSunsetChangeFlowByIdUseCase
      )

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      LocationWidgetActions.UPDATE_ALL_WIDGETS -> {
        glanceAppWidget.updateAllWidgets(context, ::updateUuid)
      }
      LocationWidgetActions.UPDATE_WIDGET -> {
        val extras =
          requireNotNull(intent.extras) { "Extras were not provided to UPDATE_WIDGET action." }
        glanceAppWidget.updateWidget(
          widgetId = extras.getInt(LocationWidgetExtras.WIDGET_ID),
          context = context,
          updateState =
            WidgetLocationIdUpdate(locationId = extras.getLong(LocationWidgetExtras.LOCATION_ID))
        )
      }
    }
  }
}
