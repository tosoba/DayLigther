package com.trm.daylighter.widget.location.daynight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.core.common.di.provider.ClassProvider
import com.trm.daylighter.core.common.di.provider.MainActivityClassProvider
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeByIdUseCase
import com.trm.daylighter.widget.location.LocationWidgetActions
import com.trm.daylighter.widget.location.LocationWidgetExtras
import com.trm.daylighter.widget.location.WidgetLocationIdUpdate
import com.trm.daylighter.widget.location.updateUuid
import com.trm.daylighter.widget.util.ext.updateAllWidgets
import com.trm.daylighter.widget.util.ext.updateWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint(BroadcastReceiver::class)
class DayNightCycleWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject
  internal lateinit var getDefaultLocationSunriseSunsetChangeUseCase:
    GetDefaultLocationSunriseSunsetChangeUseCase

  @Inject
  internal lateinit var getLocationSunriseSunsetChangeByIdUseCase:
    GetLocationSunriseSunsetChangeByIdUseCase

  @Inject @MainActivityClassProvider internal lateinit var mainActivityClassProvider: ClassProvider

  override val glanceAppWidget: DayNightCycleWidget by lazy {
    DayNightCycleWidget(
      getDefaultLocationSunriseSunsetChangeUseCase,
      getLocationSunriseSunsetChangeByIdUseCase,
      mainActivityClassProvider,
    )
  }

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
            if (extras.containsKey(LocationWidgetExtras.LOCATION_ID)) {
              WidgetLocationIdUpdate(locationId = extras.getLong(LocationWidgetExtras.LOCATION_ID))
            } else {
              ::updateUuid
            },
        )
      }
    }
  }
}
