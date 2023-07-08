package com.trm.daylighter.widget.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.util.ext.isPolarDayAtLocation
import com.trm.daylighter.core.domain.util.ext.isPolarNightAtLocation
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.widget.util.ext.antiAliasPaint
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
internal fun dayPeriodChartBitmap(change: LocationSunriseSunsetChange): Bitmap {
  val context = LocalContext.current
  val bitmap =
    with(LocalSize.current) {
      Bitmap.createBitmap(
        width.value.toPx.toInt(),
        height.value.toPx.toInt(),
        Bitmap.Config.ARGB_8888
      )
    }

  Canvas(bitmap).apply {
    val (location, today, _) = change
    drawDayPeriods(zoneId = location.zoneId, today = today, location = location)
    drawTimeLine(dateTime = ZonedDateTime.now(location.zoneId), paint = nowLinePaint(context))
  }

  return bitmap
}

private fun Canvas.drawDayPeriods(zoneId: ZoneId, today: SunriseSunset, location: Location) {
  val widthPx = width.toFloat()
  val heightPx = height.toFloat()

  val secondsInDay = Duration.ofDays(1L).seconds.toFloat()
  val durations = dayPeriodDurationsInSeconds(zoneId = zoneId, sunriseSunset = today)
  val paints = today.dayPeriodPaintsFor(location)

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
          morning18Below?.atZone(zoneId),
          morning12Below?.atZone(zoneId),
          morning6Below?.atZone(zoneId),
          sunrise?.atZone(zoneId),
          sunset?.atZone(zoneId),
          evening6Below?.atZone(zoneId),
          evening12Below?.atZone(zoneId),
          evening18Below?.atZone(zoneId),
          date.atStartOfDay(zoneId).plusDays(1L),
        )
        .map(ZonedDateTime::toInstant)
    }
  return periodInstants.indices.drop(1).map { index ->
    Duration.between(periodInstants[index - 1], periodInstants[index]).seconds.toFloat()
  }
}

private fun SunriseSunset.dayPeriodPaintsFor(location: Location): List<Paint> {
  val nightPaint = antiAliasPaint(color = nightColor.toArgb())
  if (isPolarNightAtLocation(location)) return listOf(nightPaint)

  val dayPaint = antiAliasPaint(color = dayColor.toArgb())
  if (isPolarDayAtLocation(location)) return listOf(dayPaint)

  val astronomicalTwilightPaint = antiAliasPaint(color = astronomicalTwilightColor.toArgb())
  val nauticalTwilightPaint = antiAliasPaint(color = nauticalTwilightColor.toArgb())
  val civilTwilightPaint = antiAliasPaint(color = civilTwilightColor.toArgb())

  val paints = buildList {
    if (morning18Below != null) {
      add(nightPaint)
    }
    if (morning18Below != null || morning12Below != null) {
      add(astronomicalTwilightPaint)
    }
    if (morning12Below != null || morning6Below != null) {
      add(nauticalTwilightPaint)
    }
    if (morning6Below != null || sunrise != null) {
      add(civilTwilightPaint)
    }
    if (sunrise != null && sunset != null) {
      add(dayPaint)
    }
    if (sunset != null || evening6Below != null) {
      add(civilTwilightPaint)
    }
    if (evening6Below != null || evening12Below != null) {
      add(nauticalTwilightPaint)
    }
    if (evening12Below != null || evening18Below != null) {
      add(astronomicalTwilightPaint)
    }
    if (evening18Below != null) {
      add(nightPaint)
    }
  }
  return paints.zipWithNext().filter { it.first != it.second }.map { it.first } + paints.last()
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
  antiAliasPaint(Color(context.resources.getColor(R.color.now_line, context.theme)).toArgb())
