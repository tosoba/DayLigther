package com.trm.daylighter.core.ui.composable

import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.util.ext.radians
import com.trm.daylighter.core.common.util.ext.timeDifferenceLabel
import com.trm.daylighter.core.common.util.ext.timeLabel
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.model.WithData
import com.trm.daylighter.core.domain.util.ext.isPolarDayAtLocation
import com.trm.daylighter.core.domain.util.ext.isPolarNightAtLocation
import com.trm.daylighter.core.common.util.ext.currentPeriodIn
import com.trm.daylighter.core.common.util.ext.dayPeriodEndTime
import com.trm.daylighter.core.common.util.ext.dayPeriodStartTime
import com.trm.daylighter.core.common.model.DayMode
import com.trm.daylighter.core.common.model.DayPeriod
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun DayPeriodChart(
  change: StableLoadable<LocationSunriseSunsetChange>,
  dayMode: DayMode,
  now: LocalTime,
  appBarHeightPx: Float,
  modifier: Modifier = Modifier
) {
  val orientation = LocalConfiguration.current.orientation

  val changeValue = change.value
  val location = if (changeValue is WithData) changeValue.data.location else null
  val today = if (changeValue is WithData) changeValue.data.today else null
  val yesterday = if (changeValue is WithData) changeValue.data.yesterday else null

  val chartSegments =
    dayPeriodChartSegments(
      location = location,
      today = today,
      yesterday = yesterday,
      orientation = orientation,
      using24HFormat = DateFormat.is24HourFormat(LocalContext.current)
    )

  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall
  val textColor = MaterialTheme.colorScheme.onBackground

  val dayLabel = stringResource(R.string.day)

  val nowLineColor = colorResource(id = R.color.now_line)

  Canvas(modifier = modifier) {
    val topLeftOffset =
      Offset(
        -size.height * if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.7f else 1f,
        -size.height * .5f
      )
    val segmentSize = Size(size.height, size.height) * 2f
    var startAngle = -90f

    fun DrawScope.drawChartSegment(segment: DayChartSegment) {
      clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
        drawIntoCanvas {
          val paint =
            Paint().apply {
              style = PaintingStyle.Stroke
              strokeWidth = 50f
            }
          paint.asFrameworkPaint().apply {
            color = segment.color.copy(alpha = 0f).toArgb()
            setShadowLayer(40f, 0f, 0f, segment.color.copy(alpha = .75f).toArgb())
          }
          it.drawArc(
            left = topLeftOffset.x,
            top = topLeftOffset.y,
            bottom = topLeftOffset.y + segmentSize.height,
            right = topLeftOffset.x + segmentSize.width,
            startAngle = startAngle,
            sweepAngle = segment.sweepAngleDegrees,
            useCenter = false,
            paint = paint,
          )
        }

        drawArc(
          color = segment.color,
          startAngle = startAngle,
          sweepAngle = segment.sweepAngleDegrees,
          useCenter = true,
          topLeft = topLeftOffset,
          size = segmentSize
        )

        startAngle += segment.sweepAngleDegrees
      }
    }

    chartSegments.forEach(::drawChartSegment)

    if (changeValue !is Ready) return@Canvas

    val chartRadius = segmentSize.maxDimension / 2f
    val chartCenter = Offset(topLeftOffset.x + chartRadius, size.height / 2f)
    val textPadding = 3.dp.toPx()
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val portraitLineRadiusMultiplier = 1.025f
    val landscapeLineRadiusMultiplier = 1.1f

    repeat(chartSegments.size - 1) { segmentIndex ->
      val endingEdgeAngleRadians = chartSegments[segmentIndex + 1].endingEdgeAngle.radians

      clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
        val lineRadiusMultiplier =
          when {
            chartSegments[segmentIndex].periodLabel.startsWith(dayLabel) -> 10f
            orientation == Configuration.ORIENTATION_PORTRAIT -> portraitLineRadiusMultiplier
            else -> landscapeLineRadiusMultiplier
          }
        val strokeWidth = 4f

        drawLine(
          color = chartSegments[segmentIndex + 1].color,
          start = chartCenter,
          end =
            Offset(
              x = chartCenter.x + chartRadius * lineRadiusMultiplier * cos(endingEdgeAngleRadians),
              y =
                chartCenter.y +
                  chartRadius * lineRadiusMultiplier * sin(endingEdgeAngleRadians) +
                  strokeWidth
            ),
          strokeWidth = strokeWidth,
          pathEffect =
            if (chartSegments[segmentIndex].periodLabel.startsWith(dayLabel)) null
            else dashPathEffect
        )
      }

      val textRadiusMultiplier =
        if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.025f else 1.1f
      val endingEdgeLabel =
        AnnotatedString(
          chartSegments[segmentIndex].run {
            if (dayMode == DayMode.SUNRISE) sunriseEndingEdgeLabel else sunsetEndingEdgeLabel
          }
        )
      val endingEdgeLabelLayoutResult = textMeasurer.measure(text = endingEdgeLabel)
      val endingEdgeLabelTopLeft =
        Offset(
          x =
            chartCenter.x +
              chartRadius * textRadiusMultiplier * cos(endingEdgeAngleRadians) +
              textPadding,
          y =
            chartCenter.y + chartRadius * textRadiusMultiplier * sin(endingEdgeAngleRadians) -
              if (chartSegments[segmentIndex].periodLabel.startsWith(dayLabel)) {
                0f
              } else {
                endingEdgeLabelLayoutResult.size.height /
                  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    2f
                  } else {
                    4f
                  }
              }
        )
      drawText(
        textMeasurer = textMeasurer,
        text = endingEdgeLabel,
        topLeft = endingEdgeLabelTopLeft,
        style = labelSmallTextStyle.copy(textAlign = TextAlign.Left, color = textColor),
        maxLines = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 1,
        overflow = TextOverflow.Ellipsis,
      )

      val timeAndDiffLabel = buildString {
        append(
          chartSegments[segmentIndex].run {
            requireNotNull(if (dayMode == DayMode.SUNRISE) sunriseTimeLabel else sunsetTimeLabel)
          }()
        )
        append(if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " ")
        append(
          chartSegments[segmentIndex].run {
            requireNotNull(if (dayMode == DayMode.SUNRISE) sunriseDiffLabel else sunsetDiffLabel)
          }()
        )
      }
      val timeLayoutResult = textMeasurer.measure(text = AnnotatedString(timeAndDiffLabel))
      val timeTopLeft =
        Offset(
          x =
            java.lang.Float.max(
              endingEdgeLabelTopLeft.x +
                endingEdgeLabelLayoutResult.size.width.toFloat() +
                textPadding,
              size.width - timeLayoutResult.size.width - textPadding
            ),
          y =
            chartCenter.y + chartRadius * textRadiusMultiplier * sin(endingEdgeAngleRadians) -
              if (chartSegments[segmentIndex].periodLabel.startsWith(dayLabel)) 0f
              else timeLayoutResult.size.height / 2f
        )
      drawText(
        textMeasurer = textMeasurer,
        text = timeAndDiffLabel,
        topLeft = timeTopLeft,
        style = labelSmallTextStyle.copy(textAlign = TextAlign.Right, color = textColor),
        maxLines = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    chartSegments.forEach { segment ->
      val angleDeltaDegrees = if (segment.periodLabel.startsWith(dayLabel)) 0f else 6f
      rotate(degrees = (segment.periodLabelAngle - angleDeltaDegrees / 2f), pivot = chartCenter) {
        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(segment.periodLabel))
        drawText(
          textMeasurer = textMeasurer,
          text = segment.periodLabel,
          topLeft =
            Offset(
              x = chartCenter.x + chartRadius - textLayoutResult.size.width - textPadding,
              y =
                if (segment.periodLabel.startsWith(dayLabel)) {
                  chartCenter.y - textLayoutResult.size.height - textPadding
                } else {
                  chartCenter.y - textLayoutResult.size.height / 2f
                }
            ),
          style =
            labelSmallTextStyle.copy(
              color = if (segment.periodLabel.startsWith(dayLabel)) Color.Black else Color.White,
              textAlign = TextAlign.Right
            ),
        )
      }
    }

    if (
      (now.hour < 12 && dayMode == DayMode.SUNSET) || (now.hour >= 12 && dayMode == DayMode.SUNRISE)
    ) {
      return@Canvas
    }

    clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
      val lineRadiusMultiplier =
        if (orientation == Configuration.ORIENTATION_PORTRAIT) portraitLineRadiusMultiplier
        else landscapeLineRadiusMultiplier
      val currentTimeAngleRadians =
        currentTimeLineAngleRadians(
          sunriseSunset = requireNotNull(today),
          location = requireNotNull(location),
          now = now,
          dayMode = dayMode,
          canvasHeight = size.height,
          appBarHeight = appBarHeightPx,
          chartRadius = chartRadius
        )

      drawIntoCanvas {
        val paint =
          Paint().apply {
            style = PaintingStyle.Stroke
            strokeWidth = 10f
          }
        paint.asFrameworkPaint().apply {
          color = nowLineColor.copy(alpha = 0f).toArgb()
          setShadowLayer(15f, 0f, 0f, nowLineColor.copy(alpha = .75f).toArgb())
        }
        it.drawLine(
          p1 = chartCenter,
          p2 =
            Offset(
              x = chartCenter.x + chartRadius * lineRadiusMultiplier * cos(currentTimeAngleRadians),
              y = chartCenter.y + chartRadius * lineRadiusMultiplier * sin(currentTimeAngleRadians)
            ),
          paint
        )
      }

      drawLine(
        color = nowLineColor,
        start = chartCenter,
        end =
          Offset(
            x = chartCenter.x + chartRadius * lineRadiusMultiplier * cos(currentTimeAngleRadians),
            y = chartCenter.y + chartRadius * lineRadiusMultiplier * sin(currentTimeAngleRadians)
          ),
        strokeWidth = 8f,
      )
    }
  }
}

private fun currentTimeLineAngleRadians(
  sunriseSunset: SunriseSunset,
  location: Location,
  now: LocalTime,
  dayMode: DayMode,
  canvasHeight: Float,
  appBarHeight: Float,
  chartRadius: Float,
): Float {
  val dayPeriod = sunriseSunset.currentPeriodIn(location)

  val startAngle =
    sunriseSunset.dayPeriodStartAngleRadians(
      dayPeriod = dayPeriod,
      dayMode = dayMode,
      canvasHeight = canvasHeight,
      appBarHeight = appBarHeight,
      chartRadius = chartRadius
    )
  val endAngle =
    sunriseSunset.dayPeriodEndAngleRadians(
      dayPeriod = dayPeriod,
      dayMode = dayMode,
      canvasHeight = canvasHeight,
      appBarHeight = appBarHeight,
      chartRadius = chartRadius
    )

  val startTimeSecond =
    sunriseSunset.dayPeriodStartTime(dayPeriod = dayPeriod, dayMode = dayMode).toSecondOfDay()
  val endTimeSecond =
    sunriseSunset.dayPeriodEndTime(dayPeriod = dayPeriod, dayMode = dayMode).toSecondOfDay()
  val nowSecond = now.toSecondOfDay()

  return ((endAngle - startAngle) * abs(nowSecond - startTimeSecond)) /
    abs(endTimeSecond - startTimeSecond) + startAngle
}

private fun SunriseSunset.dayPeriodStartAngleRadians(
  dayPeriod: DayPeriod,
  dayMode: DayMode,
  canvasHeight: Float,
  appBarHeight: Float,
  chartRadius: Float,
): Float {
  val nightStart = asin(canvasHeight / (2f * chartRadius))
  val dayStart = -asin((canvasHeight - appBarHeight) / (2f * chartRadius))
  return when (dayPeriod) {
    DayPeriod.NIGHT -> {
      when (dayMode) {
        DayMode.SUNRISE -> nightStart
        DayMode.SUNSET -> astronomicalTwilightEnd?.let { 18f.radians } ?: dayStart
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> astronomicalTwilightBegin?.let { 18f.radians } ?: nightStart
        DayMode.SUNSET -> nauticalTwilightEnd?.let { 12f.radians } ?: dayStart
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> nauticalTwilightBegin?.let { 12f.radians } ?: nightStart
        DayMode.SUNSET -> civilTwilightEnd?.let { 6f.radians } ?: dayStart
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> civilTwilightBegin?.let { 6f.radians } ?: nightStart
        DayMode.SUNSET -> sunset?.let { 0f.radians } ?: dayStart
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunset?.let { 0f.radians } ?: nightStart
        DayMode.SUNSET -> dayStart
      }
    }
  }
}

private fun SunriseSunset.dayPeriodEndAngleRadians(
  dayPeriod: DayPeriod,
  dayMode: DayMode,
  canvasHeight: Float,
  appBarHeight: Float,
  chartRadius: Float,
): Float {
  val nightEnd = asin(canvasHeight / (2f * chartRadius))
  val dayEnd = -asin((canvasHeight - appBarHeight) / (2f * chartRadius))
  return when (dayPeriod) {
    DayPeriod.NIGHT -> {
      when (dayMode) {
        DayMode.SUNRISE -> astronomicalTwilightBegin?.let { 18f.radians } ?: dayEnd
        DayMode.SUNSET -> nightEnd
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> nauticalTwilightBegin?.let { 12f.radians } ?: dayEnd
        DayMode.SUNSET -> astronomicalTwilightEnd?.let { 18f.radians } ?: nightEnd
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> civilTwilightBegin?.let { 6f.radians } ?: dayEnd
        DayMode.SUNSET -> nauticalTwilightEnd?.let { 12f.radians } ?: nightEnd
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunrise?.let { 0f.radians } ?: dayEnd
        DayMode.SUNSET -> civilTwilightEnd?.let { 6f.radians } ?: nightEnd
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> dayEnd
        DayMode.SUNSET -> sunset?.let { 0f.radians } ?: nightEnd
      }
    }
  }
}

private data class DayChartSegment(
  val sweepAngleDegrees: Float,
  val endingEdgeAngle: Float,
  val periodLabelAngle: Float,
  val color: Color,
  val periodLabel: String,
  val sunriseEndingEdgeLabel: String = "",
  val sunsetEndingEdgeLabel: String = "",
  val sunriseTimeLabel: (() -> String)? = null,
  val sunsetTimeLabel: (() -> String)? = null,
  val sunriseDiffLabel: (() -> String)? = null,
  val sunsetDiffLabel: (() -> String)? = null,
)

@Composable
private fun dayPeriodChartSegments(
  location: Location?,
  today: SunriseSunset?,
  yesterday: SunriseSunset?,
  orientation: Int,
  using24HFormat: Boolean
): List<DayChartSegment> {
  val sunriseLabel = stringResource(R.string.sunrise)
  val sunsetLabel = stringResource(R.string.sunset)

  val dayLabel = stringResource(R.string.day)
  val civilTwilightLabel = stringResource(R.string.civil_twilight)
  val nauticalTwilightLabel = stringResource(R.string.nautical_twilight)
  val astronomicalTwilightLabel = stringResource(R.string.astronomical_twilight)
  val nightLabel = stringResource(R.string.night)
  val longestTwilightLabelLength =
    listOf(
        dayLabel,
        civilTwilightLabel,
        nauticalTwilightLabel,
        astronomicalTwilightLabel,
        nightLabel
      )
      .maxOf(String::length)

  fun String.padToLongestLabel(): String = padEnd(longestTwilightLabelLength)

  val edgeLabelSeparator = if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "
  val civilDawnLabel = stringResource(R.string.civil_dawn_degrees_below, edgeLabelSeparator)
  val civilDuskLabel = stringResource(R.string.civil_dusk_degrees_below, edgeLabelSeparator)
  val nauticalDawnLabel = stringResource(R.string.nautical_dawn_degrees_below, edgeLabelSeparator)
  val nauticalDuskLabel = stringResource(R.string.nautical_dusk_degrees_below, edgeLabelSeparator)
  val astronomicalDawnLabel =
    stringResource(R.string.astronomical_dawn_degrees_below, edgeLabelSeparator)
  val astronomicalDuskLabel =
    stringResource(R.string.astronomical_dusk_degrees_below, edgeLabelSeparator)

  return remember(today, yesterday, using24HFormat) {
    var accumulatedSweepAngle = 0f
    buildList {
      if (
        today == null ||
          (today.sunrise != null && today.sunset != null) ||
          today.isPolarDayAtLocation(requireNotNull(location))
      ) {
        add(
          DayChartSegment(
            sweepAngleDegrees = 90f + accumulatedSweepAngle,
            endingEdgeAngle = 0f,
            periodLabelAngle = 0f,
            color = dayColor,
            periodLabel = dayLabel.padToLongestLabel(),
            sunriseEndingEdgeLabel = sunriseLabel,
            sunsetEndingEdgeLabel = sunsetLabel,
            sunriseTimeLabel = today?.sunrise?.timeLabel(using24HFormat) ?: { "" },
            sunsetTimeLabel = today?.sunset?.timeLabel(using24HFormat) ?: { "" },
            sunriseDiffLabel = {
              val yesterdaySunrise = yesterday?.sunrise
              val todaySunrise = today?.sunrise
              if (todaySunrise != null && yesterdaySunrise != null) {
                timeDifferenceLabel(yesterdaySunrise.toLocalTime(), todaySunrise.toLocalTime())
              } else {
                ""
              }
            },
            sunsetDiffLabel = {
              val yesterdaySunset = yesterday?.sunset
              val todaySunset = today?.sunset
              if (todaySunset != null && yesterdaySunset != null) {
                timeDifferenceLabel(yesterdaySunset.toLocalTime(), todaySunset.toLocalTime())
              } else {
                ""
              }
            }
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 90f
      }

      if (
        today == null ||
          (today.sunrise != null && today.sunset != null) ||
          (today.civilTwilightBegin != null && today.civilTwilightEnd != null)
      ) {
        add(
          DayChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            endingEdgeAngle = 0f,
            periodLabelAngle = 6f,
            color = civilTwilightColor,
            periodLabel = civilTwilightLabel.padToLongestLabel(),
            sunriseEndingEdgeLabel = civilDawnLabel,
            sunsetEndingEdgeLabel = civilDuskLabel,
            sunriseTimeLabel = today?.civilTwilightBegin?.timeLabel(using24HFormat) ?: { "" },
            sunsetTimeLabel = today?.civilTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
            sunriseDiffLabel = {
              val yesterdayCivilTwilightBegin = yesterday?.civilTwilightBegin
              val todayCivilTwilightBegin = today?.civilTwilightBegin
              if (yesterdayCivilTwilightBegin != null && todayCivilTwilightBegin != null) {
                timeDifferenceLabel(
                  yesterdayCivilTwilightBegin.toLocalTime(),
                  todayCivilTwilightBegin.toLocalTime()
                )
              } else {
                ""
              }
            },
            sunsetDiffLabel = {
              val yesterdayCivilTwilightEnd = yesterday?.civilTwilightEnd
              val todayCivilTwilightEnd = today?.civilTwilightEnd
              if (todayCivilTwilightEnd != null && yesterdayCivilTwilightEnd != null) {
                timeDifferenceLabel(
                  yesterdayCivilTwilightEnd.toLocalTime(),
                  todayCivilTwilightEnd.toLocalTime()
                )
              } else {
                ""
              }
            }
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        today == null ||
          (today.civilTwilightBegin != null && today.civilTwilightEnd != null) ||
          (today.nauticalTwilightBegin != null && today.nauticalTwilightEnd != null)
      ) {
        add(
          DayChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            endingEdgeAngle = 6f,
            periodLabelAngle = 12f,
            color = nauticalTwilightColor,
            periodLabel = nauticalTwilightLabel.padToLongestLabel(),
            sunriseEndingEdgeLabel = nauticalDawnLabel,
            sunsetEndingEdgeLabel = nauticalDuskLabel,
            sunriseTimeLabel = today?.nauticalTwilightBegin?.timeLabel(using24HFormat) ?: { "" },
            sunsetTimeLabel = today?.nauticalTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
            sunriseDiffLabel = {
              val todayNauticalTwilightBegin = today?.nauticalTwilightBegin
              val yesterdayNauticalTwilightBegin = yesterday?.nauticalTwilightBegin
              if (todayNauticalTwilightBegin != null && yesterdayNauticalTwilightBegin != null) {
                timeDifferenceLabel(
                  yesterdayNauticalTwilightBegin.toLocalTime(),
                  todayNauticalTwilightBegin.toLocalTime()
                )
              } else {
                ""
              }
            },
            sunsetDiffLabel = {
              val todayNauticalTwilightEnd = today?.nauticalTwilightEnd
              val yesterdayNauticalTwilightEnd = yesterday?.nauticalTwilightEnd
              if (todayNauticalTwilightEnd != null && yesterdayNauticalTwilightEnd != null) {
                timeDifferenceLabel(
                  yesterdayNauticalTwilightEnd.toLocalTime(),
                  todayNauticalTwilightEnd.toLocalTime()
                )
              } else {
                ""
              }
            }
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        today == null ||
          (today.nauticalTwilightBegin != null && today.nauticalTwilightEnd != null) ||
          (today.astronomicalTwilightBegin != null && today.astronomicalTwilightEnd != null)
      ) {
        add(
          DayChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            endingEdgeAngle = 12f,
            periodLabelAngle = 18f,
            color = astronomicalTwilightColor,
            periodLabel = astronomicalTwilightLabel.padToLongestLabel(),
            sunriseEndingEdgeLabel = astronomicalDawnLabel,
            sunsetEndingEdgeLabel = astronomicalDuskLabel,
            sunriseTimeLabel = today?.astronomicalTwilightBegin?.timeLabel(using24HFormat)
                ?: { "" },
            sunsetTimeLabel = today?.astronomicalTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
            sunriseDiffLabel = {
              val yesterdayAstronomicalTwilightBegin = yesterday?.astronomicalTwilightBegin
              val todayAstronomicalTwilightBegin = today?.astronomicalTwilightBegin
              if (
                todayAstronomicalTwilightBegin != null && yesterdayAstronomicalTwilightBegin != null
              ) {
                timeDifferenceLabel(
                  yesterdayAstronomicalTwilightBegin.toLocalTime(),
                  todayAstronomicalTwilightBegin.toLocalTime()
                )
              } else {
                ""
              }
            },
            sunsetDiffLabel = {
              val yesterdayAstronomicalTwilightEnd = yesterday?.astronomicalTwilightEnd
              val todayAstronomicalTwilightEnd = today?.astronomicalTwilightEnd
              if (
                todayAstronomicalTwilightEnd != null && yesterdayAstronomicalTwilightEnd != null
              ) {
                timeDifferenceLabel(
                  yesterdayAstronomicalTwilightEnd.toLocalTime(),
                  todayAstronomicalTwilightEnd.toLocalTime()
                )
              } else {
                ""
              }
            }
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        today == null ||
          (today.astronomicalTwilightBegin != null && today.astronomicalTwilightEnd != null) ||
          today.isPolarNightAtLocation(requireNotNull(location))
      ) {
        add(
          DayChartSegment(
            sweepAngleDegrees = 72f + accumulatedSweepAngle,
            endingEdgeAngle = 18f,
            periodLabelAngle = 24f,
            color = nightColor,
            periodLabel = nightLabel.padToLongestLabel()
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 72f
      }

      if (accumulatedSweepAngle > 0f) {
        add(
          removeLast().let {
            it.copy(sweepAngleDegrees = it.sweepAngleDegrees + accumulatedSweepAngle)
          }
        )
      }
    }
  }
}
