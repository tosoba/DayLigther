package com.trm.daylighter.widget.location

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeFlowUseCase
import com.trm.daylighter.widget.util.ext.getGlanceIds
import com.trm.daylighter.widget.util.ext.widgetReceiverIntent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject
  internal lateinit var getDefaultLocationSunriseSunsetChangeFlowUseCase:
    GetDefaultLocationSunriseSunsetChangeFlowUseCase

  override val glanceAppWidget: GlanceAppWidget by
    lazy(LazyThreadSafetyMode.NONE) {
      LocationWidget(getDefaultLocationSunriseSunsetChangeFlowUseCase)
    }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    enqueueWidgetUpdateIfAnyExists(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE -> enqueueWidgetUpdateIfAnyExists(context)
    }
  }

  private fun enqueueWidgetUpdateIfAnyExists(context: Context) {
    CoroutineScope(context = SupervisorJob() + Dispatchers.Default).launch {
      context
        .getGlanceIds<LocationWidget>()
        .map { async { glanceAppWidget.update(context, it) } }
        .awaitAll()
    }
  }

  internal companion object {
    private const val ACTION_UPDATE = "ACTION_UPDATE"

    fun updateIntent(context: Context): Intent =
      context.widgetReceiverIntent<LocationWidgetReceiver>(ACTION_UPDATE)
  }
}
