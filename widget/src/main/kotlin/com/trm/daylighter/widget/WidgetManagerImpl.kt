package com.trm.daylighter.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.widget.location.LocationWidgetExtras
import com.trm.daylighter.widget.location.LocationWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetManagerImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : WidgetManager {
  override fun updateDefaultLocationWidgets() {
    context.sendBroadcast(LocationWidgetReceiver.updateAllWidgetsIntent(context))
  }

  override suspend fun addLocationWidget(locationId: Long): Boolean =
    GlanceAppWidgetManager(context)
      .requestPinGlanceAppWidget(
        receiver = LocationWidgetReceiver::class.java,
        preview = null,
        previewState = null,
        successCallback =
          PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, LocationWidgetPinnedReceiver::class.java)
              .putExtra(LocationWidgetExtras.LOCATION_ID, locationId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
      )

  override suspend fun editLocationWidget(widgetId: Int, locationId: Long) {
    context.sendBroadcast(
      LocationWidgetReceiver.updateWidgetIntent(
        context = context,
        widgetId = widgetId,
        locationId = locationId
      )
    )
  }
}
