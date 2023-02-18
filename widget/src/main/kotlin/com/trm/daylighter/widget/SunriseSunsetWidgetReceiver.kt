package com.trm.daylighter.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SunriseSunsetWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = SunriseSunsetWidget()

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    SunriseSunsetWidgetWorker.enqueue(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    SunriseSunsetWidgetWorker.cancel(context)
  }
}
