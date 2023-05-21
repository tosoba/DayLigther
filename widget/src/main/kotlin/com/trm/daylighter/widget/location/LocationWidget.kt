package com.trm.daylighter.widget.location

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.dayLengthDiffPrefix
import com.trm.daylighter.core.common.util.ext.dayLengthDiffTime
import com.trm.daylighter.core.common.util.ext.timeZoneDiffLabelBetween
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Failed
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.usecase.GetDefaultLocationSunriseSunsetChangeFlowUseCase
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.light_onDayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.widget.R
import com.trm.daylighter.widget.ui.AddLocationButton
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.ProgressIndicator
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.ui.appWidgetBackgroundCornerRadius
import com.trm.daylighter.widget.ui.stringResource
import com.trm.daylighter.widget.ui.toPx
import com.trm.daylighter.widget.util.ext.antiAliasPaint
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LocationWidget(
  private val getDefaultLocationSunriseSunsetChangeFlowUseCase:
    GetDefaultLocationSunriseSunsetChangeFlowUseCase
) : GlanceAppWidget() {
  override val stateDefinition = LocationWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(tallMode))

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      val change by getDefaultLocationSunriseSunsetChangeFlowUseCase().collectAsState(LoadingFirst)
      Content(change = change)
    }
  }

  @Composable
  private fun Content(change: Loadable<LocationSunriseSunsetChange>) {
    GlanceTheme {
      when (change) {
        Empty -> AddLocationButton()
        is Loading -> ProgressIndicator()
        is Ready -> DayChart(change = change.data)
        is Failed -> RetryButton(onClick = updateWidgetAction())
      }
    }
  }

  @Composable
  private fun DayChart(change: LocationSunriseSunsetChange) {
    Box(
      contentAlignment = Alignment.TopEnd,
      modifier = GlanceModifier.fillMaxSize().appWidgetBackgroundCornerRadius()
    ) {
      Image(
        provider = BitmapImageProvider(dayChartBitmap(change = change)),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = GlanceModifier.fillMaxSize()
      )

      Image(
        provider = ImageProvider(commonR.drawable.refresh),
        contentDescription = stringResource(id = commonR.string.refresh),
        modifier = GlanceModifier.padding(5.dp).clickable(updateWidgetAction())
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
            NowTimezoneDiffText(dateTime = ZonedDateTime.now(change.location.zoneId))
            DayLengthInfo(today = change.today, yesterday = change.yesterday)
          }
        }
      }
    }
  }

  companion object {
    private val tallMode = DpSize(200.dp, 100.dp)
  }
}

@Composable
private fun dayChartBitmap(change: LocationSunriseSunsetChange): Bitmap {
  val context = LocalContext.current
  val size = LocalSize.current
  val widthPx = size.width.value.toPx
  val heightPx = size.height.value.toPx
  val bitmap = Bitmap.createBitmap(widthPx.toInt(), heightPx.toInt(), Bitmap.Config.ARGB_8888)
  val (location, today, _) = change

  Canvas(bitmap).apply {
    drawDayPeriods(zoneId = location.zoneId, today = change.today)
    drawTimeLine(dateTime = ZonedDateTime.now(location.zoneId), paint = nowLinePaint(context))
  }

  return bitmap
}

private fun Canvas.drawDayPeriods(zoneId: ZoneId, today: SunriseSunset) {
  val widthPx = width.toFloat()
  val heightPx = height.toFloat()

  val secondsInDay = Duration.ofDays(1L).seconds.toFloat()
  val durations = dayPeriodDurationsInSeconds(zoneId = zoneId, sunriseSunset = today)
  val paints = dayPeriodPaints()

  var left = 0f
  var right = left + durations.first() / secondsInDay * widthPx
  durations.indices.forEach { index ->
    drawRect(left, 0f, right, heightPx, paints[index])
    left += durations[index] / secondsInDay * widthPx
    if (index != durations.lastIndex) right += durations[index + 1] / secondsInDay * widthPx
    else right = widthPx
  }
}

private fun dayPeriodDurationsInSeconds(zoneId: ZoneId, sunriseSunset: SunriseSunset): List<Float> {
  val periodInstants =
    sunriseSunset.run {
      listOfNotNull(
          date.atStartOfDay(zoneId),
          astronomicalTwilightBegin?.atZone(zoneId),
          nauticalTwilightBegin?.atZone(zoneId),
          civilTwilightBegin?.atZone(zoneId),
          sunrise?.atZone(zoneId),
          sunset?.atZone(zoneId),
          civilTwilightEnd?.atZone(zoneId),
          nauticalTwilightEnd?.atZone(zoneId),
          astronomicalTwilightEnd?.atZone(zoneId),
          date.atStartOfDay(zoneId).plusDays(1L),
        )
        .map(ZonedDateTime::toInstant)
    }
  return periodInstants.indices.drop(1).map { index ->
    Duration.between(periodInstants[index - 1], periodInstants[index]).seconds.toFloat()
  }
}

private fun dayPeriodPaints(): List<Paint> {
  val nightPaint = antiAliasPaint(color = nightColor.toArgb())
  val astronomicalTwilightPaint = antiAliasPaint(color = astronomicalTwilightColor.toArgb())
  val nauticalTwilightPaint = antiAliasPaint(color = nauticalTwilightColor.toArgb())
  val civilTwilightPaint = antiAliasPaint(color = civilTwilightColor.toArgb())
  val dayPaint = antiAliasPaint(color = dayColor.toArgb())
  return listOf(
    nightPaint,
    astronomicalTwilightPaint,
    nauticalTwilightPaint,
    civilTwilightPaint,
    dayPaint,
    civilTwilightPaint,
    nauticalTwilightPaint,
    astronomicalTwilightPaint,
    nightPaint
  )
}

private fun Canvas.drawTimeLine(
  dateTime: ZonedDateTime,
  paint: Paint,
  topPx: Float = 0f,
  bottomPx: Float = height.toFloat(),
  lineWidthDp: Dp = 2.dp
) {
  val linePosition = timeXFor(dateTime)
  val lineWidthPx = lineWidthDp.value.toPx
  drawRect(linePosition - lineWidthPx / 2f, topPx, linePosition + lineWidthPx / 2f, bottomPx, paint)
}

private fun Canvas.timeXFor(dateTime: ZonedDateTime): Float {
  val secondsInDay = Duration.ofDays(1L).seconds.toFloat()
  val startOfDay = dateTime.toLocalDate().atStartOfDay(dateTime.zone)
  val secondOfDay = Duration.between(startOfDay, dateTime).seconds.toFloat()
  return secondOfDay / secondsInDay * width
}

private fun nowLinePaint(context: Context): Paint =
  antiAliasPaint(
    Color(
        with(context.resources) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getColor(commonR.color.sun_inside, context.theme)
          } else {
            getColor(commonR.color.sun_inside)
          }
        }
      )
      .toArgb()
  )

@Composable
private fun updateWidgetAction() =
  actionSendBroadcast(LocationWidgetReceiver.updateIntent(LocalContext.current))

@Composable
private fun LocationName(location: Location) {
  val context = LocalContext.current
  val size = LocalSize.current
  Box(contentAlignment = Alignment.Center, modifier = GlanceModifier.width(size.width * .75f)) {
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
          setCharSequence(R.id.shadow_text_view, "setText", location.name)
          setInt(R.id.shadow_text_view, "setTextColor", light_onDayColor.toArgb())
          setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, 12f)
        }
    )
  }
}

@Composable
private fun Clock(zoneId: ZoneId) {
  Box {
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(LocalContext.current.packageName, R.layout.location_text_clock_remote_view)
          .apply {
            setString(R.id.location_clock, "setTimeZone", zoneId.id)
            setInt(R.id.location_clock, "setTextColor", light_onDayColor.toArgb())
          }
    )
  }
}

@Composable
private fun NowTimezoneDiffText(dateTime: ZonedDateTime) {
  val context = LocalContext.current
  Box {
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
          setCharSequence(
            R.id.shadow_text_view,
            "setText",
            context.timeZoneDiffLabelBetween(ZonedDateTime.now(), dateTime)
          )
          setInt(R.id.shadow_text_view, "setTextColor", light_onDayColor.toArgb())
          setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, 12f)
        }
    )
  }
}

@Composable
private fun DayLengthInfo(today: SunriseSunset, yesterday: SunriseSunset) {
  val context = LocalContext.current
  val todayLength = LocalTime.ofSecondOfDay(today.dayLengthSeconds.toLong())
  val dayLengthDiffTime = dayLengthDiffTime(today.dayLengthSeconds, yesterday.dayLengthSeconds)
  val diffPrefix =
    dayLengthDiffPrefix(
      todayLengthSeconds = today.dayLengthSeconds,
      yesterdayLengthSeconds = yesterday.dayLengthSeconds
    )

  Row {
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
          setTextViewText(
            R.id.shadow_text_view,
            context.getString(
              R.string.day_length,
              todayLength.format(DateTimeFormatter.ISO_LOCAL_TIME)
            ),
          )
          setInt(R.id.shadow_text_view, "setTextColor", light_onDayColor.toArgb())
          setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, 14f)
        }
    )

    Text(text = " ", style = TextStyle(fontSize = 14.sp))

    AndroidRemoteViews(
      remoteViews =
        RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
          setTextViewText(
            R.id.shadow_text_view,
            context.getString(
              R.string.diff,
              diffPrefix,
              dayLengthDiffTime.minute,
              dayLengthDiffTime.second
            ),
          )
          setInt(
            R.id.shadow_text_view,
            "setTextColor",
            when (diffPrefix) {
              "+" -> Color.Green
              "-" -> Color.Red
              else -> light_onDayColor
            }.toArgb()
          )
          setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, 14f)
        }
    )
  }
}
