package com.trm.daylighter.widget.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentWidth
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.navigation.WidgetTypeParam
import com.trm.daylighter.core.common.navigation.dayNightCycleDeepLinkUri
import com.trm.daylighter.core.common.navigation.goldenBlueHourDeepLinkUri
import com.trm.daylighter.core.common.navigation.widgetLocationDeepLinkUri
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.util.ext.isPolarDayAtLocation
import com.trm.daylighter.core.domain.util.ext.isPolarNightAtLocation
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.blueHourColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.goldenHourColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.widget.R
import com.trm.daylighter.widget.location.daynight.DayNightCycleWidgetReceiver
import com.trm.daylighter.widget.location.goldenblue.GoldenBlueHourWidgetReceiver
import com.trm.daylighter.widget.util.ext.antiAliasPaint
import com.trm.daylighter.widget.util.ext.updateWidgetIntent
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

private const val WIDGET_PREVIEW_WIDTH_PX = 730
private const val WIDGET_PREVIEW_HEIGHT_PX = 190

@Composable
internal fun DayPeriodChart(
  change: LocationSunriseSunsetChange,
  chartMode: DayPeriodChartMode,
  id: GlanceId
) {
  Box(
    contentAlignment = Alignment.TopEnd,
    modifier =
      GlanceModifier.fillMaxSize()
        .appWidgetBackgroundCornerRadius()
        .clickable(widgetDayScreenDeepLinkAction(change.location, chartMode))
  ) {
    Image(
      provider = ImageProvider(dayPeriodChartBitmap(change = change, chartMode = chartMode)),
      contentDescription = null,
      contentScale = ContentScale.FillBounds,
      modifier = GlanceModifier.fillMaxSize()
    )

    Column(
      verticalAlignment = Alignment.Vertical.CenterVertically,
      horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
      modifier = GlanceModifier.fillMaxSize().appWidgetBackgroundCornerRadius()
    ) {
      LocationName(location = change.location)
      Clock(zoneId = change.location.zoneId)
      DayLengthInfo(change = change)
    }

    Column(
      verticalAlignment = Alignment.Vertical.Top,
      horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
      modifier = GlanceModifier.wrapContentWidth().fillMaxHeight().appWidgetBackgroundCornerRadius()
    ) {
      Image(
        provider = ImageProvider(R.drawable.settings),
        contentDescription = stringResource(commonR.string.settings),
        modifier =
          GlanceModifier.padding(5.dp)
            .clickable(
              widgetLocationDeepLinkAction(
                location = change.location,
                chartMode = chartMode,
                id = id
              )
            )
      )

      Spacer(GlanceModifier.defaultWeight())

      Image(
        provider = ImageProvider(R.drawable.refresh),
        contentDescription = stringResource(commonR.string.refresh),
        modifier =
          GlanceModifier.padding(5.dp)
            .clickable(widgetRefreshBroadcastAction(chartMode = chartMode, id = id))
      )
    }
  }
}

@Composable
private fun widgetDayScreenDeepLinkAction(
  location: Location,
  chartMode: DayPeriodChartMode
): Action {
  val context = LocalContext.current
  return deepLinkAction(
    uri =
      when (chartMode) {
        DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
          context.dayNightCycleDeepLinkUri(
            locationId = location.id,
            isDefault = location.isDefault,
          )
        }
        DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
          context.goldenBlueHourDeepLinkUri(
            locationId = location.id,
            isDefault = location.isDefault
          )
        }
      }
  )
}

@Composable
private fun widgetLocationDeepLinkAction(
  location: Location,
  chartMode: DayPeriodChartMode,
  id: GlanceId
): Action {
  val context = LocalContext.current
  val widgetManager = remember { GlanceAppWidgetManager(context) }
  return deepLinkAction(
    uri =
      when (chartMode) {
        DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
          context.widgetLocationDeepLinkUri(
            type = WidgetTypeParam.DAY_NIGHT_CYCLE,
            glanceId = widgetManager.getAppWidgetId(id),
            locationId = location.id
          )
        }
        DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
          context.widgetLocationDeepLinkUri(
            type = WidgetTypeParam.GOLDEN_BLUE_HOUR,
            glanceId = widgetManager.getAppWidgetId(id),
            locationId = location.id
          )
        }
      }
  )
}

@Composable
private fun widgetRefreshBroadcastAction(chartMode: DayPeriodChartMode, id: GlanceId): Action {
  val context = LocalContext.current
  val widgetManager = remember { GlanceAppWidgetManager(context) }
  return actionSendBroadcast(
    when (chartMode) {
      DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
        context.updateWidgetIntent<DayNightCycleWidgetReceiver>(widgetManager.getAppWidgetId(id))
      }
      DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
        context.updateWidgetIntent<GoldenBlueHourWidgetReceiver>(widgetManager.getAppWidgetId(id))
      }
    }
  )
}

@Composable
private fun dayPeriodChartBitmap(
  change: LocationSunriseSunsetChange,
  chartMode: DayPeriodChartMode
): Bitmap {
  val context = LocalContext.current
  val bitmap =
    with(LocalSize.current) {
      Bitmap.createBitmap(
        width.value.toPx.toInt().takeIf { it > 0 } ?: WIDGET_PREVIEW_WIDTH_PX,
        height.value.toPx.toInt().takeIf { it > 0 } ?: WIDGET_PREVIEW_HEIGHT_PX,
        Bitmap.Config.ARGB_8888
      )
    }

  Canvas(bitmap).apply {
    val (location, today, _) = change
    drawDayPeriods(today = today, location = location, chartMode = chartMode)
    drawTimeLine(dateTime = ZonedDateTime.now(location.zoneId), paint = nowLinePaint(context))
  }

  return bitmap
}

private fun Canvas.drawDayPeriods(
  today: SunriseSunset,
  location: Location,
  chartMode: DayPeriodChartMode
) {
  val widthPx = width.toFloat()
  val heightPx = height.toFloat()

  val secondsInDay = Duration.ofDays(1L).seconds.toFloat()
  val durations =
    dayPeriodDurationsInSeconds(
      sunriseSunset = today,
      zoneId = location.zoneId,
      chartMode = chartMode
    )
  val paints = today.dayPeriodPaintsFor(location = location, chartMode = chartMode)

  var left = 0f
  var right = left + durations.first() / secondsInDay * widthPx
  durations.indices.forEach { index ->
    drawRect(left, 0f, right, heightPx, paints[index])
    left += durations[index] / secondsInDay * widthPx
    if (index != durations.lastIndex) right += durations[index + 1] / secondsInDay * widthPx
    else right = widthPx
  }
}

private fun dayPeriodDurationsInSeconds(
  sunriseSunset: SunriseSunset,
  zoneId: ZoneId,
  chartMode: DayPeriodChartMode
): List<Float> {
  val periodInstants =
    sunriseSunset.run {
      buildList {
          add(date.atStartOfDay(zoneId))
          add(morning18Below?.atZone(zoneId))
          add(morning12Below?.atZone(zoneId))
          add(morning6Below?.atZone(zoneId))
          when (chartMode) {
            DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
              add(sunrise?.atZone(zoneId))
              add(sunset?.atZone(zoneId))
            }
            DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
              add(morning4Below?.atZone(zoneId))
              add(morning6Above?.atZone(zoneId))
              add(evening6Above?.atZone(zoneId))
              add(evening4Below?.atZone(zoneId))
            }
          }
          add(evening6Below?.atZone(zoneId))
          add(evening12Below?.atZone(zoneId))
          add(evening18Below?.atZone(zoneId))
          add(date.atStartOfDay(zoneId).plusDays(1L))
        }
        .filterNotNull()
        .map(ZonedDateTime::toInstant)
    }
  return periodInstants.indices.drop(1).map { index ->
    Duration.between(periodInstants[index - 1], periodInstants[index]).seconds.toFloat()
  }
}

private fun SunriseSunset.dayPeriodPaintsFor(
  location: Location,
  chartMode: DayPeriodChartMode
): List<Paint> {
  val nightPaint = antiAliasPaint(color = nightColor.toArgb())
  if (isPolarNightAtLocation(location)) return listOf(nightPaint)

  val dayPaint = antiAliasPaint(color = dayColor.toArgb())
  if (isPolarDayAtLocation(location)) return listOf(dayPaint)

  val astronomicalTwilightPaint = antiAliasPaint(color = astronomicalTwilightColor.toArgb())
  val nauticalTwilightPaint = antiAliasPaint(color = nauticalTwilightColor.toArgb())
  val civilTwilightPaint = antiAliasPaint(color = civilTwilightColor.toArgb())
  val blueHourPaint = antiAliasPaint(color = blueHourColor.toArgb())
  val goldenHourPaint = antiAliasPaint(color = goldenHourColor.toArgb())

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
    when (chartMode) {
      DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
        if (morning6Below != null || sunrise != null) {
          add(civilTwilightPaint)
        }
        if (sunrise != null && sunset != null) {
          add(dayPaint)
        }
        if (sunset != null || evening6Below != null) {
          add(civilTwilightPaint)
        }
      }
      DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
        if (morning6Below != null || morning4Below != null) {
          add(blueHourPaint)
        }
        if (morning4Below != null || morning6Above != null) {
          add(goldenHourPaint)
        }
        if (morning6Above != null || evening6Above != null) {
          add(dayPaint)
        }
        if (evening6Above != null || evening4Below != null) {
          add(goldenHourPaint)
        }
        if (evening4Below != null || evening6Below != null) {
          add(blueHourPaint)
        }
      }
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
  lineWidthDp: Dp = 3.dp
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
  antiAliasPaint(Color(context.resources.getColor(commonR.color.now_line, context.theme)).toArgb())
