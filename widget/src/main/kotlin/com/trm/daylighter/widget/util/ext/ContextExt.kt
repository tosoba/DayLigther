package com.trm.daylighter.widget.util.ext

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.widget.BuildConfig
import com.trm.daylighter.widget.R
import com.trm.daylighter.widget.location.LocationWidgetActions
import com.trm.daylighter.widget.location.LocationWidgetExtras

internal suspend inline fun <reified T : GlanceAppWidget> Context.getGlanceIds(): List<GlanceId> =
  GlanceAppWidgetManager(this).getGlanceIds(T::class.java)

internal fun Context.getGlanceIdByWidgetId(widgetId: Int): GlanceId =
  GlanceAppWidgetManager(this).getGlanceIdBy(widgetId)

private inline fun <reified T> Context.actionIntent(action: String): Intent =
  Intent(this, T::class.java).also { it.action = action }

internal fun Context.showWidgetPinnedToast() {
  Toast.makeText(this, getString(R.string.widget_pinned_to_home_screen), Toast.LENGTH_LONG).show()
}

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.updateWidgetIntent(
  widgetId: Int,
  locationId: Long? = null
): Intent =
  actionIntent<T>(LocationWidgetActions.UPDATE_WIDGET)
    .putExtra(LocationWidgetExtras.WIDGET_ID, widgetId)
    .run {
      if (locationId != null) putExtra(LocationWidgetExtras.LOCATION_ID, locationId) else this
    }

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.updateAllWidgetsIntent(): Intent =
  actionIntent<T>(LocationWidgetActions.UPDATE_ALL_WIDGETS)

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.widgetReceiverComponentName() =
  ComponentName(
    applicationContext.packageName.replace(".${BuildConfig.BUILD_TYPE}", ""),
    T::class.java.name,
  )

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.getLastWidgetId(): Int? =
  AppWidgetManager.getInstance(this).getAppWidgetIds(widgetReceiverComponentName<T>()).lastOrNull()

internal inline fun <reified T : BroadcastReceiver> Context.widgetPinSuccessCallback(
  locationId: Long
): PendingIntent =
  PendingIntent.getBroadcast(
    this,
    0,
    Intent(this, T::class.java).putExtra(LocationWidgetExtras.LOCATION_ID, locationId),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )
