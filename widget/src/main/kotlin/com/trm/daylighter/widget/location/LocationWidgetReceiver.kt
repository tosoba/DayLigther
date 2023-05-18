package com.trm.daylighter.widget.location

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.trm.daylighter.core.common.di.DaylighterDispatchers
import com.trm.daylighter.core.common.di.Dispatcher
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.widget.util.ext.anyWidgetExists
import com.trm.daylighter.widget.util.ext.widgetReceiverIntent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject internal lateinit var sunriseSunsetRepo: SunriseSunsetRepo

  @Inject
  @Dispatcher(DaylighterDispatchers.IO)
  internal lateinit var ioDispatcher: CoroutineDispatcher

  override val glanceAppWidget: GlanceAppWidget by
    lazy(LazyThreadSafetyMode.NONE) { LocationWidget(sunriseSunsetRepo, ioDispatcher) }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    LocationWidgetWorker.enqueue(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    LocationWidgetWorker.cancel(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      ACTION_UPDATE -> enqueueWidgetUpdateIfAnyExists(context)
    }
  }

  private fun enqueueWidgetUpdateIfAnyExists(context: Context) {
    MainScope().launch {
      if (context.anyWidgetExists<LocationWidget>()) {
        LocationWidgetWorker.enqueue(context)
      }
    }
  }

  internal companion object {
    private const val ACTION_UPDATE = "ACTION_UPDATE"

    fun updateIntent(context: Context): Intent =
      context.widgetReceiverIntent<LocationWidgetReceiver>(ACTION_UPDATE)
  }
}
