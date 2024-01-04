package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import com.trm.daylighter.core.ui.model.DayPeriodChartMode

@Composable
internal fun ProgressIndicator(chartMode: DayPeriodChartMode) {
  AppWidgetBox(
    modifier = GlanceModifier.fillMaxSize().chartBackground(chartMode),
    contentAlignment = Alignment.BottomCenter
  ) {
    LinearProgressIndicator(modifier = GlanceModifier.fillMaxWidth())
  }
}
