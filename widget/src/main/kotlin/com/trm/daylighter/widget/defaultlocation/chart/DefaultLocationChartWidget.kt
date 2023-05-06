package com.trm.daylighter.widget.defaultlocation.chart

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Failed
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.light_onDayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.widget.ui.AddLocationButton
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.ui.appWidgetBackgroundCornerRadius
import com.trm.daylighter.widget.ui.stringResource
import com.trm.daylighter.widget.ui.toPx
import com.trm.daylighter.widget.util.ext.antiAliasPaint
import java.time.Duration
import java.time.ZonedDateTime

class DefaultLocationChartWidget : GlanceAppWidget() {
  override val stateDefinition = DefaultLocationChartWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(shortMode, tallMode))

  @Composable
  override fun Content() {
    GlanceTheme {
      when (val change = currentState<Loadable<LocationSunriseSunsetChange>>()) {
        Empty -> AddLocationButton()
        is Loading -> CircularProgressIndicator()
        is Ready -> DayChart(change = change.data)
        is Failed -> RetryButton(onClick = updateWidgetAction())
      }
    }
  }

  companion object {
    private val shortMode = DpSize(200.dp, 50.dp)
    private val tallMode = DpSize(200.dp, 100.dp)
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
  }
}

@Composable
private fun Canvas.DrawNowLine(today: SunriseSunset) {
  val context = LocalContext.current
  DrawTimeLine(
    dateTime = ZonedDateTime.now(today.sunrise.zone),
    paint =
      antiAliasPaint(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Color(context.resources.getColor(commonR.color.sun_outline, context.theme))
          } else {
            Color(context.resources.getColor(commonR.color.sun_outline))
          }
          .toArgb()
      )
  )
}

@Composable
private fun Canvas.DrawTimeLine(
  dateTime: ZonedDateTime,
  paint: Paint,
  top: Float = 0f,
  bottom: Float = LocalSize.current.height.value.toPx,
  width: Dp = 2.dp
) {
  val widthPx = LocalSize.current.width.value.toPx
  val secondsInDay = Duration.ofDays(1L).seconds.toFloat()
  val startOfDay = dateTime.toLocalDate().atStartOfDay(dateTime.zone)
  val secondOfDay = Duration.between(startOfDay, dateTime).seconds.toFloat()
  val linePosition = secondOfDay / secondsInDay * widthPx
  val lineWidth = width.value.toPx
  drawRect(linePosition - lineWidth / 2f, top, linePosition + lineWidth / 2f, bottom, paint)
}

@Composable
private fun updateWidgetAction() =
  actionSendBroadcast(DefaultLocationChartWidgetReceiver.updateIntent(LocalContext.current))

@Composable
private fun dayChartBitmap(change: LocationSunriseSunsetChange): Bitmap {
  val size = LocalSize.current
  val widthPx = size.width.value.toPx
  val heightPx = size.height.value.toPx
  val bitmap = Bitmap.createBitmap(widthPx.toInt(), heightPx.toInt(), Bitmap.Config.ARGB_8888)
  val timelinePaint = antiAliasPaint(color = light_onDayColor.toArgb())

  Canvas(bitmap).apply {
    DrawDayPeriods(today = change.today)
    DrawNowLine(today = change.today)
    val timelineTop = heightPx - 10.dp.value.toPx
    DrawTimeLine(dateTime = change.today.sunrise, paint = timelinePaint, top = timelineTop)
    DrawTimeLine(dateTime = change.today.sunset, paint = timelinePaint, top = timelineTop)
  }

  return bitmap
}

@Composable
private fun Canvas.DrawDayPeriods(today: SunriseSunset) {
  val size = LocalSize.current
  val widthPx = size.width.value.toPx
  val heightPx = size.height.value.toPx

  val secondsInDay = Duration.ofDays(1L).seconds.toFloat()
  val durations = dayPeriodDurationsInSeconds(today)
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

private fun dayPeriodDurationsInSeconds(sunriseSunset: SunriseSunset): List<Float> {
  val (
    astronomicalTwilightBegin,
    astronomicalTwilightEnd,
    civilTwilightBegin,
    civilTwilightEnd,
    _,
    nauticalTwilightBegin,
    nauticalTwilightEnd,
    _,
    sunrise,
    sunset,
    date) =
    sunriseSunset
  val periodInstants =
    listOf(
        date.atStartOfDay(sunrise.zone),
        astronomicalTwilightBegin,
        nauticalTwilightBegin,
        civilTwilightBegin,
        sunrise,
        sunset,
        civilTwilightEnd,
        nauticalTwilightEnd,
        astronomicalTwilightEnd,
        date.atStartOfDay(sunrise.zone).plusDays(1L),
      )
      .map(ZonedDateTime::toInstant)
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
