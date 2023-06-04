package com.trm.daylighter.widget.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeFlowByIdUseCase
import com.trm.daylighter.widget.util.ext.actionIntent
import com.trm.daylighter.widget.util.ext.getGlanceIds
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject
  internal lateinit var getDefaultLocationSunriseSunsetChangeFlowUseCase:
    GetDefaultLocationSunriseSunsetChangeFlowUseCase

  @Inject
  internal lateinit var getLocationSunriseSunsetChangeFlowByIdUseCase:
    GetLocationSunriseSunsetChangeFlowByIdUseCase

  override val glanceAppWidget: GlanceAppWidget
    get() =
      LocationWidget(
        getDefaultLocationSunriseSunsetChangeFlowUseCase,
        getLocationSunriseSunsetChangeFlowByIdUseCase
      )

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE_ALL_WIDGETS -> {
        context.updateAllWidgets()
      }
      ACTION_UPDATE_WIDGET -> {
        val extras =
          requireNotNull(intent.extras) { "Extras were not provided to ACTION_UPDATE_WIDGET." }
        context.updateWidget(
          widgetId = extras.getInt(LocationWidgetExtras.WIDGET_ID),
          locationId = extras.getLong(LocationWidgetExtras.LOCATION_ID)
        )
      }
    }
  }

  private fun Context.updateAllWidgets() {
    CoroutineScope(context = SupervisorJob() + Dispatchers.Default).launch {
      for (id in getGlanceIds<LocationWidget>()) {
        glanceAppWidget.update(this@updateAllWidgets, id)
      }
    }
  }

  private fun Context.updateWidget(widgetId: Int, locationId: Long) {
    CoroutineScope(context = SupervisorJob() + Dispatchers.Default).launch {
      val glanceId = GlanceAppWidgetManager(this@updateWidget).getGlanceIdBy(widgetId)
      updateAppWidgetState(this@updateWidget, LocationWidgetStateDefinition, glanceId) {
        LocationWidgetState.ChosenLocation(locationId)
      }
      glanceAppWidget.update(this@updateWidget, glanceId)
    }
  }

  internal companion object {
    private const val ACTION_UPDATE_ALL_WIDGETS = "ACTION_UPDATE_ALL_WIDGETS"
    private const val ACTION_UPDATE_WIDGET = "ACTION_UPDATE_WIDGET"

    fun componentName(context: Context): ComponentName =
      ComponentName(context.applicationContext.packageName, LocationWidgetReceiver::class.java.name)

    fun updateAllWidgetsIntent(context: Context): Intent =
      context.actionIntent<LocationWidgetReceiver>(ACTION_UPDATE_ALL_WIDGETS)

    fun updateWidgetIntent(context: Context, widgetId: Int, locationId: Long): Intent =
      context
        .actionIntent<LocationWidgetReceiver>(ACTION_UPDATE_WIDGET)
        .putExtra(LocationWidgetExtras.WIDGET_ID, widgetId)
        .putExtra(LocationWidgetExtras.LOCATION_ID, locationId)
  }
}
