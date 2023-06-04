package com.trm.daylighter.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.trm.daylighter.widget.location.LocationWidgetExtras
import com.trm.daylighter.widget.location.LocationWidgetReceiver

class LocationWidgetPinnedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    context.sendBroadcast(
      LocationWidgetReceiver.updateWidgetIntent(
        context = context,
        widgetId =
          AppWidgetManager.getInstance(context)
            .getAppWidgetIds(LocationWidgetReceiver.componentName(context))
            .maxOrNull()
            ?: throw IllegalArgumentException("Did not find any glance ids for LocationWidget."),
        locationId = intent.extras?.getLong(LocationWidgetExtras.LOCATION_ID, -1L)
            ?: throw IllegalArgumentException("LOCATION_ID extra was not passed to LocationWidget.")
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
