package com.trm.daylighter.widget

import android.content.Context
import com.trm.daylighter.core.domain.widget.WidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetManagerImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : WidgetManager {
  override fun enqueueDefaultLocationWidgetUpdate() {
    context.sendBroadcast(DefaultLocationSunriseSunsetWidgetReceiver.updateIntent(context))
  }
}
