package com.trm.daylighter.widget

import android.content.Intent
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.*
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.dayLengthDiffPrefix
import com.trm.daylighter.core.common.util.ext.dayLengthDiffTime
import com.trm.daylighter.core.common.util.ext.formatTimeDifference
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.widget.ui.*
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DefaultLocationSunriseSunsetWidget : GlanceAppWidget() {
  override val stateDefinition = DefaultLocationSunriseSunsetWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(smallMode, wideMode, squareMode))

  @Composable
  override fun Content() {
    val change = currentState<Loadable<LocationSunriseSunsetChange>>()
    GlanceTheme {
      when (change) {
        is Empty -> AddLocationButton()
        is Loading -> CircularProgressIndicator()
        is Ready -> {
          val (location, today, yesterday) = change.data
          when (LocalSize.current) {
            smallMode -> DayLengthSmall(today = today, yesterday = yesterday)
            wideMode -> DayLengthWide(location = location, today = today, yesterday = yesterday)
            squareMode -> DayLengthSquare(location = location, today = today, yesterday = yesterday)
          }
        }
        is Failed -> RetryButton()
      }
    }
  }

  @Composable
  private fun DayLengthSquare(location: Location, today: SunriseSunset, yesterday: SunriseSunset) {
    AppWidgetColumn(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Spacer(modifier = GlanceModifier.height(5.dp))
      Clock(zoneId = location.zoneId)
      Spacer(modifier = GlanceModifier.height(5.dp))
      Row(
        modifier = GlanceModifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        DayLengthSymbol()
        DayLengthInfo(today = today, yesterday = yesterday)
      }
      Spacer(modifier = GlanceModifier.height(5.dp))
    }
  }

  @Composable
  private fun DayLengthWide(location: Location, today: SunriseSunset, yesterday: SunriseSunset) {
    AppWidgetRow(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Spacer(modifier = GlanceModifier.width(5.dp))
      Clock(zoneId = location.zoneId)
      Spacer(modifier = GlanceModifier.width(5.dp))
      DayLengthSymbol()
      DayLengthInfo(today = today, yesterday = yesterday)
      Spacer(modifier = GlanceModifier.width(5.dp))
    }
  }

  companion object {
    private val smallMode = DpSize(120.dp, 50.dp)
    private val wideMode = DpSize(200.dp, 50.dp)
    private val squareMode = DpSize(120.dp, 120.dp)
  }
}

@Composable
private fun Clock(zoneId: ZoneId) {
  Box {
    val context = LocalContext.current
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(LocalContext.current.packageName, R.layout.location_text_clock_remote_view)
          .apply {
            setString(R.id.location_clock, "setTimeZone", zoneId.id)
            setInt(
              R.id.location_clock,
              "setTextColor",
              GlanceTheme.colors.textColorPrimary.getColor(context).toArgb()
            )
          }
    )
  }
}

@Composable
private fun DayLengthSmall(today: SunriseSunset, yesterday: SunriseSunset) {
  AppWidgetRow(
    verticalAlignment = Alignment.CenterVertically,
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
  ) {
    DayLengthSymbol()
    DayLengthInfo(today, yesterday)
  }
}

@Composable
private fun DayLengthSymbol() {
  Box(modifier = GlanceModifier.size(50.dp), contentAlignment = Alignment.BottomEnd) {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Image(
        provider = ImageProvider(commonR.drawable.sun),
        contentDescription = null,
        modifier = GlanceModifier.size(40.dp),
      )
    }
    Box(modifier = GlanceModifier.wrapContentSize(), contentAlignment = Alignment.Center) {
      Image(provider = ImageProvider(commonR.drawable.clock), contentDescription = null)
    }
  }
}

@Composable
private fun DayLengthInfo(today: SunriseSunset, yesterday: SunriseSunset) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val todayLength = LocalTime.ofSecondOfDay(today.dayLengthSeconds.toLong())
    val dayLengthDiffTime = dayLengthDiffTime(today.dayLengthSeconds, yesterday.dayLengthSeconds)
    val diffPrefix =
      dayLengthDiffPrefix(
        todayLengthSeconds = today.dayLengthSeconds,
        yesterdayLengthSeconds = yesterday.dayLengthSeconds
      )
    val textStyle = TextStyle(color = GlanceTheme.colors.textColorPrimary)
    Text(text = todayLength.format(DateTimeFormatter.ISO_LOCAL_TIME), style = textStyle)
    Text(text = formatTimeDifference(diffPrefix, dayLengthDiffTime), style = textStyle)
  }
}

@Composable
private fun AddLocationButton() {
  AppWidgetBox(contentAlignment = Alignment.Center) {
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
}

@Composable
private fun RetryButton() {
  AppWidgetBox(contentAlignment = Alignment.Center) {
    Button(
      text = stringResource(id = commonR.string.retry),
      onClick =
        actionSendBroadcast(
          DefaultLocationSunriseSunsetWidgetReceiver.updateIntent(LocalContext.current)
        )
    )
  }
}
