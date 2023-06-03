package com.trm.daylighter.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.widget.location.LocationWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import timber.log.Timber

class WidgetManagerImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : WidgetManager {
  override fun updateDefaultLocationWidgets() {
    context.sendBroadcast(LocationWidgetReceiver.updateIntent(context))
  }

  override fun addLocationWidget(locationId: Long) {
    val widgetManager = AppWidgetManager.getInstance(context)

    val componentName =
      ComponentName(context.applicationContext.packageName, LocationWidgetReceiver::class.java.name)
    val widgetInfo =
      widgetManager.installedProviders.firstOrNull { it.provider == componentName }
        ?: run {
          Timber.e(
            "Widget id not found for package: ${componentName.packageName}; class: ${componentName.className}"
          )
          return
        }

    val successCallback =
      PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, WidgetPinnedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

    // TODO:
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      AppWidgetManager.getInstance(context)
        .requestPinAppWidget(widgetInfo.provider, null, successCallback)
    }
  }
}
