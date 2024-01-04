package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.navigation.newLocationDeeplinkUri
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.widget.R

@Composable
internal fun NewLocationButton(chartMode: DayPeriodChartMode) {
  val context = LocalContext.current
  AppWidgetBox(
    modifier = GlanceModifier.fillMaxSize().chartBackground(chartMode),
    contentAlignment = Alignment.Center
  ) {
    Button(
      text = stringResource(commonR.string.new_location),
      onClick = deepLinkAction(context.newLocationDeeplinkUri())
    )
  }
}

@Composable
internal fun GlanceModifier.chartBackground(chartMode: DayPeriodChartMode): GlanceModifier =
  then(
    GlanceModifier.background(
      ImageProvider(
        when (chartMode) {
          DayPeriodChartMode.DAY_NIGHT_CYCLE -> R.drawable.day_night_cycle_widget_background
          DayPeriodChartMode.GOLDEN_BLUE_HOUR -> R.drawable.golden_hour_widget_background
        }
      )
    )
  )
