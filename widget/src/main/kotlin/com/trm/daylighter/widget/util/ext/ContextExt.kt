package com.trm.daylighter.widget.util.ext

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
  locationId: Long
): Intent =
  actionIntent<T>(LocationWidgetActions.UPDATE_WIDGET)
    .putExtra(LocationWidgetExtras.WIDGET_ID, widgetId)
    .putExtra(LocationWidgetExtras.LOCATION_ID, locationId)

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.updateAllWidgetsIntent(): Intent =
  actionIntent<T>(LocationWidgetActions.UPDATE_ALL_WIDGETS)

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.widgetReceiverComponentName():
  ComponentName = ComponentName(applicationContext.packageName, T::class.java.name)

internal inline fun <reified T : GlanceAppWidgetReceiver> Context.getLastWidgetId(): Int? =
  AppWidgetManager.getInstance(this).getAppWidgetIds(widgetReceiverComponentName<T>()).lastOrNull()
