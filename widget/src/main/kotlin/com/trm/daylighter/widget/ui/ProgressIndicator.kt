package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import com.trm.daylighter.core.ui.model.DayPeriodChartMode

@Composable
internal fun ProgressIndicator(chartMode: DayPeriodChartMode) {
  AppWidgetBox(
    modifier = GlanceModifier.fillMaxSize().chartBackground(chartMode),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(color = GlanceTheme.colors.onPrimary)
  }
}
