package com.trm.daylighter.widget.util.ext

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager

suspend inline fun <reified T : GlanceAppWidget> Context.getGlanceIds() =
  GlanceAppWidgetManager(this).getGlanceIds(T::class.java)

suspend inline fun <reified T : GlanceAppWidget> Context.anyWidgetExists(): Boolean =
  getGlanceIds<T>().any()

inline fun <reified T> Context.widgetReceiverIntent(action: String): Intent =
  Intent(this, T::class.java).apply { this.action = action }
