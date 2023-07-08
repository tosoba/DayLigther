package com.trm.daylighter.widget.location.daynight

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.trm.daylighter.widget.R
import com.trm.daylighter.widget.location.LocationWidgetExtras

class DayNightCycleWidgetPinnedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    context.sendBroadcast(
      DayNightCycleWidgetReceiver.updateWidgetIntent(
        context = context,
        widgetId =
          AppWidgetManager.getInstance(context)
            .getAppWidgetIds(DayNightCycleWidgetReceiver.componentName(context))
            .lastOrNull()
            ?: throw IllegalArgumentException(
              "Did not find any glance ids for DayNightCycleWidget."
            ),
        locationId = intent.extras?.getLong(LocationWidgetExtras.LOCATION_ID, -1L)
            ?: throw IllegalArgumentException(
              "LOCATION_ID extra was not passed to DayNightCycleWidget."
            )
      )
    )

    Toast.makeText(
        context,
        context.getString(R.string.widget_pinned_to_home_screen),
        Toast.LENGTH_LONG
      )
      .show()
  }
}
