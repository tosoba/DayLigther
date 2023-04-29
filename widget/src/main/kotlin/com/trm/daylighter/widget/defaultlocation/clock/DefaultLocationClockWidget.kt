package com.trm.daylighter.widget.defaultlocation.clock

import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.util.ext.dayLengthDiffPrefix
import com.trm.daylighter.core.common.util.ext.dayLengthDiffTime
import com.trm.daylighter.core.common.util.ext.formatTimeDifference
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Failed
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.widget.ui.AddLocationButton
import com.trm.daylighter.widget.ui.AppWidgetColumn
import com.trm.daylighter.widget.ui.AppWidgetRow
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.ui.deepLinkAction
import com.trm.daylighter.widget.ui.isNightMode
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DefaultLocationClockWidget : GlanceAppWidget() {
  override val stateDefinition = DefaultLocationClockWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(smallMode, wideMode, squareMode))

  @Composable
  override fun Content() {
    GlanceTheme {
      when (val change = currentState<Loadable<LocationSunriseSunsetChange>>()) {
        Empty -> {
          AddLocationButton()
        }
        is Loading -> {
          CircularProgressIndicator()
        }
        is Ready -> {
          val (location, today, yesterday) = change.data
          when (LocalSize.current) {
            smallMode -> {
              DayLengthSmall(
                today = today,
                yesterday = yesterday,
                modifier = clickableDayDeepLinkModifier()
              )
            }
            wideMode -> {
              DayLengthWide(
                location = location,
                today = today,
                yesterday = yesterday,
                modifier = clickableDayDeepLinkModifier()
              )
            }
            squareMode -> {
              DayLengthSquare(
                location = location,
                today = today,
                yesterday = yesterday,
                modifier = clickableDayDeepLinkModifier()
              )
            }
          }
        }
        is Failed -> {
          RetryButton(
            onClick =
              actionSendBroadcast(
                DefaultLocationClockWidgetReceiver.updateIntent(LocalContext.current)
              )
          )
        }
      }
    }
  }

  @Composable
  private fun clickableDayDeepLinkModifier() =
    GlanceModifier.clickable(deepLinkAction(R.string.day_deep_link_uri))

  companion object {
    private val smallMode = DpSize(120.dp, 50.dp)
    private val wideMode = DpSize(200.dp, 50.dp)
    private val squareMode = DpSize(120.dp, 120.dp)
  }
}

@Composable
private fun DayLengthSmall(
  today: SunriseSunset,
  yesterday: SunriseSunset,
  modifier: GlanceModifier = GlanceModifier
) {
  AppWidgetRow(
    verticalAlignment = Alignment.CenterVertically,
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    modifier = modifier
  ) {
    DayLengthSymbol(modifier = GlanceModifier.size(40.dp))
    DayLengthInfo(today = today, yesterday = yesterday, modifier = GlanceModifier.defaultWeight())
  }
}

@Composable
private fun DayLengthSquare(
  location: Location,
  today: SunriseSunset,
  yesterday: SunriseSunset,
  modifier: GlanceModifier = GlanceModifier
) {
  AppWidgetColumn(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
  ) {
    Spacer(modifier = GlanceModifier.height(5.dp))
    Clock(zoneId = location.zoneId)
    Spacer(modifier = GlanceModifier.height(5.dp))
    Row(
      modifier = GlanceModifier.padding(horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      DayLengthSymbol(modifier = GlanceModifier.size(50.dp))
      DayLengthInfo(today = today, yesterday = yesterday, modifier = GlanceModifier.defaultWeight())
    }
    Spacer(modifier = GlanceModifier.height(5.dp))
  }
}

@Composable
private fun DayLengthWide(
  location: Location,
  today: SunriseSunset,
  yesterday: SunriseSunset,
  modifier: GlanceModifier = GlanceModifier
) {
  AppWidgetRow(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
  ) {
    Spacer(modifier = GlanceModifier.width(5.dp))
    Clock(zoneId = location.zoneId)
    Spacer(modifier = GlanceModifier.width(5.dp))
    DayLengthSymbol(modifier = GlanceModifier.size(50.dp))
    DayLengthInfo(today = today, yesterday = yesterday, modifier = GlanceModifier.wrapContentSize())
    Spacer(modifier = GlanceModifier.width(5.dp))
  }
}

@Composable
private fun Clock(zoneId: ZoneId) {
  Box {
    val context = LocalContext.current
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(
            context.packageName,
            com.trm.daylighter.widget.R.layout.location_text_clock_remote_view
          )
          .apply {
            setString(com.trm.daylighter.widget.R.id.location_clock, "setTimeZone", zoneId.id)
            setInt(
              com.trm.daylighter.widget.R.id.location_clock,
              "setTextColor",
              GlanceTheme.colors.textColorPrimary.getColor(context).toArgb()
            )
          }
    )
  }
}

@Composable
private fun DayLengthSymbol(modifier: GlanceModifier = GlanceModifier) {
  val context = LocalContext.current
  Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
    Box(modifier = GlanceModifier.padding(5.dp), contentAlignment = Alignment.Center) {
      Image(
        provider = ImageProvider(R.drawable.sun),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxSize()
      )
    }
    Box(modifier = GlanceModifier.size(20.dp), contentAlignment = Alignment.Center) {
      Image(
        provider =
          if (context.isNightMode) ImageProvider(com.trm.daylighter.widget.R.drawable.clock_white)
          else ImageProvider(com.trm.daylighter.widget.R.drawable.clock_black),
        contentDescription = null
      )
    }
  }
}

@Composable
private fun DayLengthInfo(
  today: SunriseSunset,
  yesterday: SunriseSunset,
  modifier: GlanceModifier = GlanceModifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
  ) {
    val todayLength = LocalTime.ofSecondOfDay(today.dayLengthSeconds.toLong())
    val dayLengthDiffTime = dayLengthDiffTime(today.dayLengthSeconds, yesterday.dayLengthSeconds)
    val diffPrefix =
      dayLengthDiffPrefix(
        todayLengthSeconds = today.dayLengthSeconds,
        yesterdayLengthSeconds = yesterday.dayLengthSeconds
      )
    val textStyle = TextStyle(color = GlanceTheme.colors.textColorPrimary)
    Text(
      text = todayLength.format(DateTimeFormatter.ISO_LOCAL_TIME),
      style = textStyle,
      maxLines = 1
    )
    Text(
      text = formatTimeDifference(diffPrefix, dayLengthDiffTime),
      style = textStyle,
      maxLines = 1
    )
  }
}
