package com.trm.daylighter.widget.util.ext

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager

suspend inline fun <reified T : GlanceAppWidget> Context.getGlanceIds(): List<GlanceId> =
  GlanceAppWidgetManager(this).getGlanceIds(T::class.java)

inline fun <reified T> Context.widgetReceiverIntent(action: String): Intent =
  Intent(this, T::class.java).apply { this.action = action }
