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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.model.DayMode
import com.trm.daylighter.core.common.model.DayPeriod
import com.trm.daylighter.core.common.util.ext.currentPeriodIn
import com.trm.daylighter.core.common.util.ext.dayPeriodEndTime
import com.trm.daylighter.core.common.util.ext.dayPeriodStartTime
import com.trm.daylighter.core.common.util.ext.radians
import com.trm.daylighter.core.common.util.ext.timeDifferenceLabel
import com.trm.daylighter.core.common.util.ext.timeLabel
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.model.dataOrNull
import com.trm.daylighter.core.domain.util.ext.isPolarDayAtLocation
import com.trm.daylighter.core.domain.util.ext.isPolarNightAtLocation
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import java.lang.Float.max
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

private const val portraitLineRadiusMultiplier = 1.025f
private const val landscapeLineRadiusMultiplier = 1.1f

private val DrawScope.chartSegmentSize: Size
  get() = Size(size.height, size.height) * 2f

private val DrawScope.chartRadius: Float
  get() = chartSegmentSize.maxDimension / 2f

private fun DrawScope.chartTopLeftOffset(orientation: Int): Offset =
  Offset(
    x = -size.height * if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.7f else 1f,
    y = -size.height * .5f
  )

private fun DrawScope.chartCenter(orientation: Int): Offset =
  Offset(x = chartTopLeftOffset(orientation).x + chartRadius, y = size.height / 2f)

private val DrawScope.chartTextPaddingPx: Float
  get() = 3.dp.toPx()

@OptIn(ExperimentalTextApi::class)
@Composable
fun DayPeriodChart(
  change: StableLoadable<LocationSunriseSunsetChange>,
  modifier: Modifier = Modifier,
  dayMode: DayMode = DayMode.SUNRISE,
  now: LocalTime = LocalTime.now(),
  appBarHeightPx: Float = 0f
) {
  val orientation = LocalConfiguration.current.orientation

  val changeValue = change.value

  val chartSegments = dayLengthPeriodChartSegments(change = changeValue.dataOrNull())

  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall
  val textColor = MaterialTheme.colorScheme.onBackground

  val dayLabel = stringResource(R.string.day)
  val nowLineColor = colorResource(id = R.color.now_line)

  Canvas(modifier = modifier) {
    val topLeftOffset = chartTopLeftOffset(orientation)
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
            bottom = topLeftOffset.y + chartSegmentSize.height,
            right = topLeftOffset.x + chartSegmentSize.width,
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
          size = chartSegmentSize
        )

        startAngle += segment.sweepAngleDegrees
      }
    }

    chartSegments.forEach(::drawChartSegment)

    if (changeValue !is Ready) return@Canvas

    val chartCenter = chartCenter(orientation)
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
            else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
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
              chartTextPaddingPx,
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

      val timeAndDiffLabel =
        buildTimeAndDiffLabel(
          chartSegment = chartSegments[segmentIndex],
          dayMode = dayMode,
          orientation = orientation
        )
      val timeLayoutResult = textMeasurer.measure(text = AnnotatedString(timeAndDiffLabel))
      val timeTopLeft =
        Offset(
          x =
            max(
              endingEdgeLabelTopLeft.x +
                endingEdgeLabelLayoutResult.size.width.toFloat() +
                chartTextPaddingPx,
              size.width - timeLayoutResult.size.width - chartTextPaddingPx
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

    drawPeriodLabels(
      chartSegments = chartSegments,
      textMeasurer = textMeasurer,
      textStyle = labelSmallTextStyle,
      dayLabel = dayLabel,
      orientation = orientation
    )

    if (
      (now.hour < 12 && dayMode == DayMode.SUNSET) || (now.hour >= 12 && dayMode == DayMode.SUNRISE)
    ) {
      return@Canvas
    }

    drawNowLine(
      today = changeValue.data.today,
      location = changeValue.data.location,
      now = now,
      dayMode = dayMode,
      nowLineColor = nowLineColor,
      orientation = orientation,
      appBarHeightPx = appBarHeightPx,
    )
  }
}

private fun buildTimeAndDiffLabel(
  chartSegment: DayChartSegment,
  dayMode: DayMode,
  orientation: Int
): String = buildString {
  append(
    chartSegment.run {
      requireNotNull(if (dayMode == DayMode.SUNRISE) sunriseTimeLabel else sunsetTimeLabel)
    }()
  )
  append(if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " ")
  append(
    chartSegment.run {
      requireNotNull(if (dayMode == DayMode.SUNRISE) sunriseDiffLabel else sunsetDiffLabel)
    }
  )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawPeriodLabels(
  chartSegments: List<DayChartSegment>,
  textMeasurer: TextMeasurer,
  textStyle: TextStyle,
  dayLabel: String,
  orientation: Int,
) {
  val chartCenter = chartCenter(orientation)

  chartSegments.forEach { segment ->
    val angleDeltaDegrees = if (segment.periodLabel.startsWith(dayLabel)) 0f else 6f
    rotate(degrees = (segment.periodLabelAngle - angleDeltaDegrees / 2f), pivot = chartCenter) {
      val textLayoutResult = textMeasurer.measure(text = AnnotatedString(segment.periodLabel))
      drawText(
        textMeasurer = textMeasurer,
        text = segment.periodLabel,
        topLeft =
          Offset(
            x = chartCenter.x + chartRadius - textLayoutResult.size.width - chartTextPaddingPx,
            y =
              if (segment.periodLabel.startsWith(dayLabel)) {
                chartCenter.y - textLayoutResult.size.height - chartTextPaddingPx
              } else {
                chartCenter.y - textLayoutResult.size.height / 2f
              }
          ),
        style =
          textStyle.copy(
            color = if (segment.periodLabel.startsWith(dayLabel)) Color.Black else Color.White,
            textAlign = TextAlign.Right
          ),
      )
    }
  }
}

private fun DrawScope.drawNowLine(
  today: SunriseSunset,
  location: Location,
  now: LocalTime,
  dayMode: DayMode,
  nowLineColor: Color,
  orientation: Int,
  appBarHeightPx: Float,
) {
  val chartCenter = chartCenter(orientation)

  clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
    val lineRadiusMultiplier =
      if (orientation == Configuration.ORIENTATION_PORTRAIT) portraitLineRadiusMultiplier
      else landscapeLineRadiusMultiplier
    val currentTimeAngleRadians =
      currentTimeLineAngleRadians(
        sunriseSunset = today,
        location = location,
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
  val sunriseDiffLabel: String? = null,
  val sunsetDiffLabel: String? = null,
)

@Composable
private fun dayLengthPeriodChartSegments(
  change: LocationSunriseSunsetChange?
): List<DayChartSegment> {
  val orientation = LocalConfiguration.current.orientation
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

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

  return remember(change, using24HFormat) {
    var accumulatedSweepAngle = 0f
    buildList {
      if (
        change == null ||
          (change.today.sunrise != null && change.today.sunset != null) ||
          change.today.isPolarDayAtLocation(change.location)
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
            sunriseTimeLabel = change?.today?.sunrise?.timeLabel(using24HFormat) ?: { "" },
            sunsetTimeLabel = change?.today?.sunset?.timeLabel(using24HFormat) ?: { "" },
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.sunrise,
                todayTimestamp = change?.today?.sunrise
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.sunset,
                todayTimestamp = change?.today?.sunset
              )
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 90f
      }

      if (
        change == null ||
          (change.today.sunrise != null && change.today.sunset != null) ||
          (change.today.civilTwilightBegin != null && change.today.civilTwilightEnd != null)
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
            sunriseTimeLabel = change?.today?.civilTwilightBegin?.timeLabel(using24HFormat)
                ?: { "" },
            sunsetTimeLabel = change?.today?.civilTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.civilTwilightBegin,
                todayTimestamp = change?.today?.civilTwilightBegin
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.civilTwilightEnd,
                todayTimestamp = change?.today?.civilTwilightEnd
              )
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.civilTwilightBegin != null && change.today.civilTwilightEnd != null) ||
          (change.today.nauticalTwilightBegin != null && change.today.nauticalTwilightEnd != null)
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
            sunriseTimeLabel = change?.today?.nauticalTwilightBegin?.timeLabel(using24HFormat)
                ?: { "" },
            sunsetTimeLabel = change?.today?.nauticalTwilightEnd?.timeLabel(using24HFormat)
                ?: { "" },
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.nauticalTwilightBegin,
                todayTimestamp = change?.today?.nauticalTwilightBegin
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.nauticalTwilightEnd,
                todayTimestamp = change?.today?.nauticalTwilightEnd
              )
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.nauticalTwilightBegin != null &&
            change.today.nauticalTwilightEnd != null) ||
          (change.today.astronomicalTwilightBegin != null &&
            change.today.astronomicalTwilightEnd != null)
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
            sunriseTimeLabel = change?.today?.astronomicalTwilightBegin?.timeLabel(using24HFormat)
                ?: { "" },
            sunsetTimeLabel = change?.today?.astronomicalTwilightEnd?.timeLabel(using24HFormat)
                ?: { "" },
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.astronomicalTwilightBegin,
                todayTimestamp = change?.today?.astronomicalTwilightBegin
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change?.yesterday?.astronomicalTwilightEnd,
                todayTimestamp = change?.today?.astronomicalTwilightEnd
              )
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.astronomicalTwilightBegin != null &&
            change.today.astronomicalTwilightEnd != null) ||
          change.today.isPolarNightAtLocation(change.location)
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

private fun timestampDiffLabel(
  yesterdayTimestamp: LocalDateTime?,
  todayTimestamp: LocalDateTime?
): String =
  if (yesterdayTimestamp != null && todayTimestamp != null) {
    timeDifferenceLabel(yesterdayTimestamp.toLocalTime(), todayTimestamp.toLocalTime())
  } else {
    ""
  }
