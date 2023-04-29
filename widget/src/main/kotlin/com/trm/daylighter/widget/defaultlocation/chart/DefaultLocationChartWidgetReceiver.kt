package com.trm.daylighter.widget.defaultlocation.chart

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
class DefaultLocationChartWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = DefaultLocationChartWidget()

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    DefaultLocationChartWidgetWorker.enqueue(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    DefaultLocationChartWidgetWorker.cancel(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE -> enqueueWidgetUpdateIfAnyExists(context)
    }
  }

  private fun enqueueWidgetUpdateIfAnyExists(context: Context) {
    MainScope().launch {
      if (context.anyWidgetExists<DefaultLocationChartWidget>()) {
        DefaultLocationChartWidgetWorker.enqueue(context)
      }
    }
  }

  internal companion object {
    private const val ACTION_UPDATE = "ACTION_UPDATE"

    fun updateIntent(context: Context): Intent =
      context.widgetReceiverIntent<DefaultLocationChartWidgetReceiver>(ACTION_UPDATE)
  }
}
