package com.trm.daylighter.widget.location.goldenblue

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Failed
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeByIdUseCase
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.widget.location.locationIdKey
import com.trm.daylighter.widget.ui.DayPeriodChart
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.NewLocationButton
import com.trm.daylighter.widget.ui.ProgressIndicator
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.util.ext.updateWidgetIntent

class GoldenBlueHourWidget(
  private val getDefaultLocationSunriseSunsetChangeUseCase:
    GetDefaultLocationSunriseSunsetChangeUseCase,
  private val getLocationSunriseSunsetChangeByIdUseCase: GetLocationSunriseSunsetChangeByIdUseCase
) : GlanceAppWidget() {
  override val sizeMode: SizeMode = SizeMode.Exact

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      val state = currentState<Preferences>()
      val change by
        produceState<Loadable<LocationSunriseSunsetChange>>(LoadingFirst, state) {
          val locationId = state[locationIdKey]
          value =
            if (locationId == null) getDefaultLocationSunriseSunsetChangeUseCase()
            else getLocationSunriseSunsetChangeByIdUseCase(locationId)
        }
      GoldenBlueHourContent(change = change, id = id)
    }
  }
}

class GoldenBlueHourWidgetPreview(
  private val change: Loadable<LocationSunriseSunsetChange>,
) : GlanceAppWidget() {
  override val sizeMode: SizeMode = SizeMode.Exact

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent { GoldenBlueHourContent(change = change, id = id) }
  }
}

@Composable
private fun GoldenBlueHourContent(change: Loadable<LocationSunriseSunsetChange>, id: GlanceId) {
  GlanceTheme {
    when (change) {
      Empty -> {
        NewLocationButton(DayPeriodChartMode.GOLDEN_BLUE_HOUR)
      }
      is Loading -> {
        ProgressIndicator(DayPeriodChartMode.GOLDEN_BLUE_HOUR)
      }
      is Ready -> {
        DayPeriodChart(
          change = change.data,
          chartMode = DayPeriodChartMode.GOLDEN_BLUE_HOUR,
          id = id
        )
      }
      is Failed -> {
        RetryButton(id = id)
      }
    }
  }
}

@Composable
private fun RetryButton(id: GlanceId) {
  val context = LocalContext.current
  val widgetManager = remember(id) { GlanceAppWidgetManager(context) }
  RetryButton(
    chartMode = DayPeriodChartMode.GOLDEN_BLUE_HOUR,
    onClick =
      actionSendBroadcast(
        context.updateWidgetIntent<GoldenBlueHourWidgetReceiver>(widgetManager.getAppWidgetId(id))
      )
  )
}
