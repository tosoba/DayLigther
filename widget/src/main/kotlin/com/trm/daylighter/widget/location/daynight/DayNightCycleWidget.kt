package com.trm.daylighter.widget.location.daynight

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.trm.daylighter.core.common.di.provider.ClassProvider
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
import com.trm.daylighter.widget.ui.LocalClassProvider
import com.trm.daylighter.widget.ui.LocalIsPreviewProvider
import com.trm.daylighter.widget.ui.NewLocationButton
import com.trm.daylighter.widget.ui.ProgressIndicator
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.util.ext.updateWidgetIntent

class DayNightCycleWidget(
  private val getDefaultLocationSunriseSunsetChangeUseCase:
    GetDefaultLocationSunriseSunsetChangeUseCase,
  private val getLocationSunriseSunsetChangeByIdUseCase: GetLocationSunriseSunsetChangeByIdUseCase,
  private val mainActivityClassProvider: ClassProvider,
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
      CompositionLocalProvider(
        LocalClassProvider provides mainActivityClassProvider,
        LocalIsPreviewProvider provides false,
      ) {
        DayNightCycleContent(change = change, id = id)
      }
    }
  }
}

class DayNightCycleWidgetPreview(private val change: Loadable<LocationSunriseSunsetChange>) :
  GlanceAppWidget() {
  override val sizeMode: SizeMode = SizeMode.Exact

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      CompositionLocalProvider(LocalIsPreviewProvider provides true) {
        DayNightCycleContent(change = change, id = id)
      }
    }
  }
}

@Composable
private fun DayNightCycleContent(change: Loadable<LocationSunriseSunsetChange>, id: GlanceId) {
  GlanceTheme {
    when (change) {
      Empty -> {
        NewLocationButton(DayPeriodChartMode.DAY_NIGHT_CYCLE)
      }
      is Loading -> {
        ProgressIndicator(DayPeriodChartMode.DAY_NIGHT_CYCLE)
      }
      is Ready -> {
        DayPeriodChart(
          change = change.data,
          chartMode = DayPeriodChartMode.DAY_NIGHT_CYCLE,
          id = id,
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
    chartMode = DayPeriodChartMode.DAY_NIGHT_CYCLE,
    onClick =
      actionSendBroadcast(
        context.updateWidgetIntent<DayNightCycleWidgetReceiver>(widgetManager.getAppWidgetId(id))
      ),
  )
}
