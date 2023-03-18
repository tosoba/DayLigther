package com.trm.daylighter.widget

import android.content.Intent
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.Button
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.theme.DayLighterTheme
import com.trm.daylighter.widget.util.AppWidgetBox
import com.trm.daylighter.widget.util.stringResource
import java.time.ZoneId

class DefaultLocationSunriseSunsetWidget : GlanceAppWidget() {
  override val stateDefinition = DefaultLocationSunriseSunsetWidgetStateDefinition

  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(smallMode, wideMode, squareMode))

  @Composable
  override fun Content() {
    val loadable = currentState<Loadable<LocationSunriseSunsetChange>>()
    DayLighterTheme(darkTheme = false, tweakStatusBarAppearance = false) {
      AppWidgetBox(contentAlignment = Alignment.Center) {
        when (loadable) {
          is Empty -> AddLocationButton()
          is Loading -> CircularProgressIndicator()
          is Ready -> Clock(zoneId = loadable.data.location.zoneId)
          is Failed -> RetryButton()
        }
      }
    }
  }

  companion object {
    private val smallMode = DpSize(120.dp, 46.dp)
    private val wideMode = DpSize(200.dp, 46.dp)
    private val squareMode = DpSize(120.dp, 120.dp)
  }
}

@Composable
private fun Clock(zoneId: ZoneId) {
  AndroidRemoteViews(
    remoteViews =
      RemoteViews(LocalContext.current.packageName, R.layout.location_text_clock_remote_view)
        .apply { setString(R.id.location_clock, "setTimeZone", zoneId.id) }
  )
}

@Composable
private fun AddLocationButton() {
  Button(
    text = stringResource(id = commonR.string.add_location),
    onClick =
      actionStartActivity(
        Intent(
          Intent.ACTION_VIEW,
          stringResource(commonR.string.add_location_deep_link_uri).toUri()
        )
      )
  )
}

@Composable
private fun RetryButton() {
  Button(
    text = stringResource(id = commonR.string.retry),
    onClick =
      actionSendBroadcast(
        DefaultLocationSunriseSunsetWidgetReceiver.updateIntent(LocalContext.current)
      )
  )
}
