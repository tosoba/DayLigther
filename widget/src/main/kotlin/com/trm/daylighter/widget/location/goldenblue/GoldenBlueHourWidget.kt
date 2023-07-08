package com.trm.daylighter.widget.location.goldenblue

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.AndroidResourceImageProvider
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.navigation.goldenBlueHourDeepLinkUri
import com.trm.daylighter.core.common.navigation.widgetLocationDeepLinkUri
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Failed
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeFlowByIdUseCase
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.widget.R
import com.trm.daylighter.widget.location.locationIdKey
import com.trm.daylighter.widget.ui.AddLocationButton
import com.trm.daylighter.widget.ui.Clock
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.LocationName
import com.trm.daylighter.widget.ui.ProgressIndicator
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.ui.appWidgetBackgroundCornerRadius
import com.trm.daylighter.widget.ui.dayPeriodChartBitmap
import com.trm.daylighter.widget.ui.deepLinkAction
import com.trm.daylighter.widget.ui.stringResource
import com.trm.daylighter.widget.util.ext.updateAllWidgetsIntent

class GoldenBlueHourWidget(
  private val getDefaultLocationSunriseSunsetChangeFlowUseCase:
    GetDefaultLocationSunriseSunsetChangeFlowUseCase,
  private val getLocationSunriseSunsetChangeFlowByIdUseCase:
    GetLocationSunriseSunsetChangeFlowByIdUseCase
) : GlanceAppWidget() {
  override val stateDefinition = PreferencesGlanceStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(tallMode))

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      val state = currentState<Preferences>()
      val change by
        remember(state) {
            val locationId = state[locationIdKey]
            if (locationId == null) getDefaultLocationSunriseSunsetChangeFlowUseCase()
            else getLocationSunriseSunsetChangeFlowByIdUseCase(locationId)
          }
          .collectAsState(initial = LoadingFirst)
      Content(change = change, id = id)
    }
  }

  @Composable
  private fun Content(change: Loadable<LocationSunriseSunsetChange>, id: GlanceId) {
    GlanceTheme {
      when (change) {
        Empty -> AddLocationButton()
        is Loading -> ProgressIndicator()
        is Ready -> DayChart(change = change.data, id = id)
        is Failed ->
          RetryButton(
            onClick =
              actionSendBroadcast(
                LocalContext.current.updateAllWidgetsIntent<GoldenBlueHourWidgetReceiver>()
              )
          )
      }
    }
  }

  @Composable
  private fun DayChart(change: LocationSunriseSunsetChange, id: GlanceId) {
    val context = LocalContext.current
    val widgetManager = remember(id) { GlanceAppWidgetManager(context) }

    Box(
      contentAlignment = Alignment.TopEnd,
      modifier =
        GlanceModifier.fillMaxSize()
          .appWidgetBackgroundCornerRadius()
          .clickable(
            deepLinkAction(
              context.goldenBlueHourDeepLinkUri(
                locationId = change.location.id,
                isDefault = change.location.isDefault
              )
            )
          )
    ) {
      Image(
        provider =
          BitmapImageProvider(
            dayPeriodChartBitmap(change = change, chartMode = DayPeriodChartMode.GOLDEN_BLUE_HOUR)
          ),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = GlanceModifier.fillMaxSize()
      )

      when (LocalSize.current) {
        tallMode -> {
          Column(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier.fillMaxSize().appWidgetBackgroundCornerRadius()
          ) {
            LocationName(location = change.location)
            Clock(zoneId = change.location.zoneId)
          }
        }
      }

      Image(
        provider = AndroidResourceImageProvider(R.drawable.settings),
        contentDescription = stringResource(commonR.string.settings),
        modifier =
          GlanceModifier.padding(5.dp)
            .clickable(
              deepLinkAction(
                context.widgetLocationDeepLinkUri(
                  glanceId = widgetManager.getAppWidgetId(id),
                  locationId = change.location.id
                )
              )
            )
      )
    }
  }

  companion object {
    private val tallMode = DpSize(200.dp, 100.dp)
  }
}
