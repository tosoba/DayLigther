package com.trm.daylighter.widget.location.daynight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trm.daylighter.widget.location.LocationWidgetExtras
import com.trm.daylighter.widget.util.ext.getLastWidgetId
import com.trm.daylighter.widget.util.ext.showWidgetPinnedToast
import com.trm.daylighter.widget.util.ext.updateWidgetIntent

class DayNightCycleWidgetPinnedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    context.sendBroadcast(
      context.updateWidgetIntent<DayNightCycleWidgetReceiver>(
        widgetId = context.getLastWidgetId<DayNightCycleWidgetReceiver>()
            ?: throw IllegalArgumentException(
              "Did not find any widget ids for ${DayNightCycleWidget::class.java.simpleName}."
            ),
        locationId = intent.extras?.getLong(LocationWidgetExtras.LOCATION_ID, -1L)
            ?: throw IllegalArgumentException(
              "LOCATION_ID extra was not passed to ${DayNightCycleWidget::class.java.simpleName}."
            )
      )
    )
    context.showWidgetPinnedToast()
  }
}
