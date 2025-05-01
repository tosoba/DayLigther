package com.trm.daylighter.widget.location.goldenblue

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trm.daylighter.widget.location.LocationWidgetExtras
import com.trm.daylighter.widget.util.ext.getLastWidgetId
import com.trm.daylighter.widget.util.ext.showWidgetPinnedToast
import com.trm.daylighter.widget.util.ext.updateWidgetIntent

class GoldenBlueHourWidgetPinnedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    context.sendBroadcast(
      context.updateWidgetIntent<GoldenBlueHourWidgetReceiver>(
        widgetId =
          context.getLastWidgetId<GoldenBlueHourWidgetReceiver>()
            ?: throw IllegalArgumentException(
              "Did not find any widget ids for ${GoldenBlueHourWidget::class.java.simpleName}."
            ),
        locationId =
          intent.extras?.getLong(LocationWidgetExtras.LOCATION_ID, -1L)
            ?: throw IllegalArgumentException(
              "LOCATION_ID extra was not passed to ${GoldenBlueHourWidget::class.java.simpleName}."
            ),
      )
    )
    context.showWidgetPinnedToast()
  }
}
