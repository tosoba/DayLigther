package com.trm.daylighter.widget.locations

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.widget.util.ext.anyWidgetExists
import com.trm.daylighter.widget.util.ext.widgetReceiverIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationsSunriseSunsetWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = LocationsSunriseSunsetWidget()

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    LocationsSunriseSunsetWidgetWorker.enqueue(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    LocationsSunriseSunsetWidgetWorker.cancel(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE -> enqueueWidgetUpdateIfAnyExists(context)
    }
  }

  private fun enqueueWidgetUpdateIfAnyExists(context: Context) {
    MainScope().launch {
      if (context.anyWidgetExists<LocationsSunriseSunsetWidget>()) {
        LocationsSunriseSunsetWidgetWorker.enqueue(context)
      }
    }
  }

  internal companion object {
    private const val ACTION_UPDATE = "ACTION_UPDATE"

    fun updateIntent(context: Context): Intent =
      context.widgetReceiverIntent<LocationsSunriseSunsetWidgetReceiver>(ACTION_UPDATE)
  }
}
