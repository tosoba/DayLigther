package com.trm.daylighter.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.core.common.di.MainActivityIntent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DefaultLocationSunriseSunsetWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject @MainActivityIntent internal lateinit var mainActivityIntent: Intent

  override val glanceAppWidget: GlanceAppWidget by lazy {
    DefaultLocationSunriseSunsetWidget(mainActivityIntent)
  }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    DefaultLocationSunriseSunsetWidgetWorker.enqueue(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    DefaultLocationSunriseSunsetWidgetWorker.cancel(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE -> enqueueWidgetUpdateIfAnyExists(context)
    }
  }

  private fun enqueueWidgetUpdateIfAnyExists(context: Context) {
    MainScope().launch {
      val anyWidgetExists =
        GlanceAppWidgetManager(context)
          .getGlanceIds(DefaultLocationSunriseSunsetWidget::class.java)
          .any()
      if (anyWidgetExists) {
        DefaultLocationSunriseSunsetWidgetWorker.enqueue(context)
      }
    }
  }

  internal companion object {
    private const val ACTION_UPDATE = "ACTION_UPDATE"

    fun updateIntent(context: Context): Intent =
      Intent(context, DefaultLocationSunriseSunsetWidgetReceiver::class.java).apply {
        action = ACTION_UPDATE
      }
  }
}
