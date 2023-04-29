package com.trm.daylighter.widget

import android.content.Context
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.widget.defaultlocation.chart.DefaultLocationChartWidgetReceiver
import com.trm.daylighter.widget.defaultlocation.clock.DefaultLocationClockWidgetReceiver
import com.trm.daylighter.widget.list.clock.LocationsClockListWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetManagerImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : WidgetManager {
  override fun enqueueDefaultLocationWidgetsUpdate() {
    context.sendBroadcast(DefaultLocationClockWidgetReceiver.updateIntent(context))
    context.sendBroadcast(DefaultLocationChartWidgetReceiver.updateIntent(context))
  }

  override fun enqueueLocationsListWidgetsUpdate() {
    context.sendBroadcast(LocationsClockListWidgetReceiver.updateIntent(context))
  }
}
