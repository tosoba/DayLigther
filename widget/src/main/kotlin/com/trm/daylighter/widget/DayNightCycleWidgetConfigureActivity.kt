package com.trm.daylighter.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.trm.daylighter.core.common.navigation.WidgetType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DayNightCycleWidgetConfigureActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setResult(RESULT_CANCELED)

    val widgetId =
      intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()

    setContent {
      WidgetConfigureContent(
        widgetId = widgetId,
        widgetType = WidgetType.DAY_NIGHT_CYCLE,
        onConfirmEditWidgetLocationClick = {
          setResult(RESULT_OK)
          finish()
        },
      )
    }
  }
}
