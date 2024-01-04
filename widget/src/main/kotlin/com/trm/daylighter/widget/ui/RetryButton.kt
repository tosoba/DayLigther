package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.ui.model.DayPeriodChartMode

@Composable
internal fun RetryButton(chartMode: DayPeriodChartMode, onClick: Action) {
  AppWidgetBox(
    modifier = GlanceModifier.fillMaxSize().chartBackground(chartMode),
    contentAlignment = Alignment.Center
  ) {
    Button(text = stringResource(R.string.retry), onClick = onClick)
  }
}
