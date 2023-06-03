package com.trm.daylighter.widget.location

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeFlowByIdUseCase
import com.trm.daylighter.widget.util.ext.getGlanceIds
import com.trm.daylighter.widget.util.ext.widgetReceiverIntent
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

  override val glanceAppWidget: GlanceAppWidget by
    lazy(LazyThreadSafetyMode.NONE) {
      LocationWidget(
        getDefaultLocationSunriseSunsetChangeFlowUseCase,
        getLocationSunriseSunsetChangeFlowByIdUseCase
      )
    }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    updateExistingWidgets(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE -> updateExistingWidgets(context)
    }
  }

  private fun updateExistingWidgets(context: Context) {
    CoroutineScope(context = SupervisorJob() + Dispatchers.Default).launch {
      for (id in context.getGlanceIds<LocationWidget>()) {
        glanceAppWidget.update(context, id)
      }
    }
  }

  internal companion object {
    private const val ACTION_UPDATE = "ACTION_UPDATE"

    fun updateIntent(context: Context): Intent =
      context.widgetReceiverIntent<LocationWidgetReceiver>(ACTION_UPDATE)
  }
}
