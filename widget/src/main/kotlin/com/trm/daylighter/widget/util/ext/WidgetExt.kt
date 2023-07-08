package com.trm.daylighter.widget.util.ext

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal fun GlanceAppWidget.updateWidget(
  widgetId: Int,
  context: Context,
  updateState: suspend (MutablePreferences) -> Unit,
) {
  CoroutineScope(context = SupervisorJob() + Dispatchers.Default).launch {
    val glanceId = context.getGlanceIdByWidgetId(widgetId)
    updateAppWidgetState(context = context, glanceId = glanceId, updateState = updateState)
    update(context, glanceId)
  }
}

internal inline fun <reified T : GlanceAppWidget> T.updateAllWidgets(
  context: Context,
  noinline updateState: suspend (MutablePreferences) -> Unit,
) {
  CoroutineScope(context = SupervisorJob() + Dispatchers.Default).launch {
    for (glanceId in context.getGlanceIds<T>()) {
      updateAppWidgetState(context = context, glanceId = glanceId, updateState = updateState)
      update(context, glanceId)
    }
  }
}
