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
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.blueHourColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.goldenHourColor
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
  get() = 10.dp.toPx()

private fun chartTextRadiusMultiplier(orientation: Int): Float =
  if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.025f else 1.1f

@OptIn(ExperimentalTextApi::class)
@Composable
fun DayPeriodChart(
  change: StableLoadable<LocationSunriseSunsetChange>,
  modifier: Modifier = Modifier,
  chartMode: DayPeriodChartMode = DayPeriodChartMode.DAY_NIGHT_CYCLE,
  dayMode: DayMode = DayMode.SUNRISE,
  now: LocalTime = LocalTime.now(),
  appBarHeightPx: Float = 0f
) {
  val changeValue = change.value
  val chartSegments =
    when (chartMode) {
      DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
        dayNightCycleChartSegments(change = changeValue.dataOrNull(), dayMode = dayMode)
      }
      DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
        goldenBlueHourChartSegments(change = changeValue.dataOrNull(), dayMode = dayMode)
      }
    }

  val textMeasurer = rememberTextMeasurer()
  val labelTextStyle = MaterialTheme.typography.labelSmall
  val textColor = MaterialTheme.colorScheme.onBackground

  val orientation = LocalConfiguration.current.orientation
  val dayLabel = stringResource(R.string.day)
  val nowLineColor = colorResource(R.color.now_line)

  Canvas(modifier = modifier) {
    drawChartSegments(chartSegments = chartSegments, orientation = orientation)
    if (changeValue !is Ready) return@Canvas

    repeat(chartSegments.size - 1) { segmentIndex ->
      drawEndingEdge(
        chartSegments = chartSegments,
        segmentIndex = segmentIndex,
        dayLabel = dayLabel,
        orientation = orientation,
      )

      drawEndingEdgeAndTimeDiffLabels(
        chartSegment = chartSegments[segmentIndex],
        textMeasurer = textMeasurer,
        textStyle = labelTextStyle.copy(color = textColor),
        endingEdgeAngleRadians = chartSegments[segmentIndex + 1].endingEdgeAngleDegrees.radians,
        dayMode = dayMode,
        dayLabel = dayLabel,
        orientation = orientation
      )
    }

    drawPeriodLabels(
      chartSegments = chartSegments,
      textMeasurer = textMeasurer,
      textStyle = labelTextStyle,
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

private fun DrawScope.drawChartSegments(chartSegments: List<DayChartSegment>, orientation: Int) {
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
}

private fun DrawScope.drawEndingEdge(
  chartSegments: List<DayChartSegment>,
  segmentIndex: Int,
  dayLabel: String,
  orientation: Int
) {
  clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
    val chartCenter = chartCenter(orientation)
    val endingEdgeAngleRadians = chartSegments[segmentIndex + 1].endingEdgeAngleDegrees.radians
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
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawEndingEdgeAndTimeDiffLabels(
  chartSegment: DayChartSegment,
  textMeasurer: TextMeasurer,
  textStyle: TextStyle,
  endingEdgeAngleRadians: Float,
  dayMode: DayMode,
  dayLabel: String,
  orientation: Int
) {
  val chartCenter = chartCenter(orientation)
  val textRadiusMultiplier = chartTextRadiusMultiplier(orientation)

  val endingEdgeLabel = AnnotatedString(chartSegment.endingEdgeLabel)
  val endingEdgeTextStyle = textStyle.copy(textAlign = TextAlign.Left)
  val endingEdgeLabelLayoutResult =
    textMeasurer.measure(
      text = endingEdgeLabel,
      style = endingEdgeTextStyle,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1
    )
  val endingEdgeLabelTopLeft =
    Offset(
      x =
        chartCenter.x +
          chartRadius * textRadiusMultiplier * cos(endingEdgeAngleRadians) +
          chartTextPaddingPx,
      y =
        chartCenter.y + chartRadius * textRadiusMultiplier * sin(endingEdgeAngleRadians) -
          if (chartSegment.periodLabel.startsWith(dayLabel)) {
            0f
          } else {
            endingEdgeLabelLayoutResult.size.height / 4f
          }
    )
  drawText(
    textMeasurer = textMeasurer,
    text = endingEdgeLabel,
    topLeft = endingEdgeLabelTopLeft,
    style = endingEdgeTextStyle,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )

  val timeAndDiffLabel =
    AnnotatedString(
      buildTimeAndDiffLabel(
        chartSegment = chartSegment,
        dayMode = dayMode,
        orientation = orientation
      )
    )
  if (timeAndDiffLabel.isBlank()) return

  val timeAndDiffLabelTextStyle = textStyle.copy(textAlign = TextAlign.Right)
  val timeAndDiffLabelMaxLines = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 1
  val timeLayoutResult =
    textMeasurer.measure(
      text = timeAndDiffLabel,
      style = timeAndDiffLabelTextStyle,
      overflow = TextOverflow.Ellipsis,
      maxLines = timeAndDiffLabelMaxLines
    )
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
          if (chartSegment.periodLabel.startsWith(dayLabel)) {
            0f
          } else {
            timeLayoutResult.size.height / 4f
          }
    )
  drawText(
    textMeasurer = textMeasurer,
    text = timeAndDiffLabel,
    topLeft = timeTopLeft,
    style = timeAndDiffLabelTextStyle,
    maxLines = timeAndDiffLabelMaxLines,
    overflow = TextOverflow.Ellipsis,
  )
}

private fun buildTimeAndDiffLabel(
  chartSegment: DayChartSegment,
  dayMode: DayMode,
  orientation: Int
): String {
  val timeLabel =
    chartSegment.run { if (dayMode == DayMode.SUNRISE) sunriseTimeLabel else sunsetTimeLabel }
  if (timeLabel.isBlank()) return ""
  val diffLabel =
    chartSegment.run { if (dayMode == DayMode.SUNRISE) sunriseDiffLabel else sunsetDiffLabel }
  if (diffLabel.isBlank()) return timeLabel
  return buildString {
    append(timeLabel)
    append(if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " ")
    append(diffLabel)
  }
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
    rotate(degrees = segment.periodLabelAngleDegrees, pivot = chartCenter) {
      val periodLabel = AnnotatedString(segment.periodLabel)
      val periodLabelTextStyle =
        textStyle.copy(
          color = if (segment.periodLabel.startsWith(dayLabel)) Color.Black else Color.White,
          textAlign = TextAlign.Right
        )
      val periodLabelLayoutResult =
        textMeasurer.measure(
          text = periodLabel,
          style = periodLabelTextStyle,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
      drawText(
        textMeasurer = textMeasurer,
        text = segment.periodLabel,
        topLeft =
          Offset(
            x =
              chartCenter.x + chartRadius - periodLabelLayoutResult.size.width - chartTextPaddingPx,
            y =
              if (segment.periodLabel.startsWith(dayLabel)) {
                chartCenter.y - periodLabelLayoutResult.size.height - chartTextPaddingPx
              } else {
                chartCenter.y - periodLabelLayoutResult.size.height / 2f
              }
          ),
        style = periodLabelTextStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
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
  val endingEdgeAngleDegrees: Float,
  val periodLabelAngleDegrees: Float,
  val color: Color,
  val periodLabel: String,
  val endingEdgeLabel: String = "",
  val sunriseTimeLabel: String = "",
  val sunsetTimeLabel: String = "",
  val sunriseDiffLabel: String = "",
  val sunsetDiffLabel: String = "",
)

@Composable
private fun dayNightCycleChartSegments(
  change: LocationSunriseSunsetChange?,
  dayMode: DayMode,
): List<DayChartSegment> {
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

  val sunriseLabel = stringResource(R.string.sunrise)
  val sunsetLabel = stringResource(R.string.sunset)

  val dayLabel = stringResource(R.string.day)
  val civilTwilightLabel = stringResource(R.string.civil_twilight)
  val nauticalTwilightLabel = stringResource(R.string.nautical_twilight)
  val astronomicalTwilightLabel = stringResource(R.string.astronomical_twilight)
  val nightLabel = stringResource(R.string.night)

  val civilDawnLabel = stringResource(R.string.six_degrees_below)
  val nauticalDawnLabel = stringResource(R.string.twelve_degrees_below)
  val astronomicalDawnLabel = stringResource(R.string.eighteen_degrees_below)

  return remember(change, dayMode, using24HFormat) {
    var accumulatedSweepAngle = 0f
    buildList {
      if (
        change == null ||
          (change.today.sunrise != null && change.today.sunset != null) ||
          change.today.isPolarDayAtLocation(change.location)
      ) {
        add(
          dayChartSegment(
            change = change,
            sweepAngleDegrees = 90f + accumulatedSweepAngle,
            periodLabelAngle = 0f,
            periodLabel = dayLabel,
            endingEdgeLabel =
              when (dayMode) {
                DayMode.SUNRISE -> sunriseLabel
                DayMode.SUNSET -> sunsetLabel
              },
            using24HFormat = using24HFormat,
            showDiffLabels = true,
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
          civilTwilightChartSegment(
            change = change,
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = civilTwilightLabel,
            endingEdgeLabel = civilDawnLabel,
            using24HFormat = using24HFormat,
            showDiffLabels = true,
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
          nauticalTwilightChartSegment(
            change = change,
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = nauticalTwilightLabel,
            endingEdgeLabel = nauticalDawnLabel,
            using24HFormat = using24HFormat,
            showDiffLabels = true,
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
          astronomicalTwilightChartSegment(
            change = change,
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = astronomicalTwilightLabel,
            endingEdgeLabel = astronomicalDawnLabel,
            using24HFormat = using24HFormat,
            showDiffLabels = true,
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
          nightChartSegment(
            sweepAngleDegrees = 72f + accumulatedSweepAngle,
            periodLabelAngle =
              if (change?.today?.isPolarNightAtLocation(change.location) == true) 0f else 21f,
            periodLabel = nightLabel
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

@Composable
private fun goldenBlueHourChartSegments(
  change: LocationSunriseSunsetChange?,
  dayMode: DayMode,
): List<DayChartSegment> {
  val orientation = LocalConfiguration.current.orientation
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

  val sunriseLabel = stringResource(R.string.sunrise)
  val sunsetLabel = stringResource(R.string.sunset)

  val dayLabel = stringResource(R.string.day)
  val goldenHourLabel = stringResource(R.string.golden_hour)
  val blueHourLabel = stringResource(R.string.blue_hour)
  val nauticalTwilightLabel = stringResource(R.string.nautical_twilight)
  val astronomicalTwilightLabel = stringResource(R.string.astronomical_twilight)
  val nightLabel = stringResource(R.string.night)

  val edgeLabelSeparator = if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "
  val sixDegreesLabel = stringResource(R.string.six_degrees_below, edgeLabelSeparator)
  val twelveDegreesLabel = stringResource(R.string.twelve_degrees_below, edgeLabelSeparator)
  val eighteenDegreesLabel = stringResource(R.string.eighteen_degrees_below, edgeLabelSeparator)

  return remember(change, dayMode, using24HFormat) {
    var accumulatedSweepAngle = 0f
    buildList {
      if (
        change == null ||
          (change.today.sunrise != null && change.today.sunset != null) ||
          change.today.isPolarDayAtLocation(change.location)
      ) {
        add(
          dayChartSegment(
            change = change,
            sweepAngleDegrees = 84f + accumulatedSweepAngle,
            periodLabelAngle =
              if (change?.today?.isPolarDayAtLocation(change.location) == true) 0f else -9f,
            periodLabel = dayLabel,
            endingEdgeLabel =
              when (dayMode) {
                DayMode.SUNRISE -> sunriseLabel
                DayMode.SUNSET -> sunsetLabel
              },
            using24HFormat = using24HFormat,
            showDiffLabels = false
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 84f
      }

      if (
        change == null ||
          (change.today.goldenHourAboveMorning != null &&
            change.today.goldenHourAboveMorning != null) ||
          (change.today.goldenHourBelowMorning != null &&
            change.today.goldenHourBelowMorning != null)
      ) {
        add(
          goldenHourChartSegment(
            change = change,
            sweepAngleDegrees = 10f + accumulatedSweepAngle,
            periodLabel = goldenHourLabel,
            endingEdgeLabel = "",
            using24HFormat = using24HFormat,
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 10f
      }

      if (
        change == null ||
          (change.today.goldenHourBelowMorning != null &&
            change.today.goldenHourBelowMorning != null) ||
          (change.today.blueHourBegin != null && change.today.blueHourEnd != null)
      ) {
        add(
          blueHourChartSegment(
            change = change,
            sweepAngleDegrees = 2f + accumulatedSweepAngle,
            periodLabel = blueHourLabel,
            endingEdgeLabel = sixDegreesLabel,
            using24HFormat = using24HFormat
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 2f
      }

      if (
        change == null ||
          (change.today.civilTwilightBegin != null && change.today.civilTwilightEnd != null) ||
          (change.today.nauticalTwilightBegin != null && change.today.nauticalTwilightEnd != null)
      ) {
        add(
          nauticalTwilightChartSegment(
            change = change,
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = nauticalTwilightLabel,
            endingEdgeLabel = twelveDegreesLabel,
            using24HFormat = using24HFormat,
            showDiffLabels = false
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
          astronomicalTwilightChartSegment(
            change = change,
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = astronomicalTwilightLabel,
            endingEdgeLabel = eighteenDegreesLabel,
            using24HFormat = using24HFormat,
            showDiffLabels = false
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
          nightChartSegment(
            sweepAngleDegrees = 72f + accumulatedSweepAngle,
            periodLabelAngle =
              if (change?.today?.isPolarNightAtLocation(change.location) == true) 0f else 21f,
            periodLabel = nightLabel
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

private fun dayChartSegment(
  change: LocationSunriseSunsetChange?,
  sweepAngleDegrees: Float,
  periodLabelAngle: Float,
  periodLabel: String,
  endingEdgeLabel: String,
  using24HFormat: Boolean,
  showDiffLabels: Boolean,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = 0f,
    periodLabelAngleDegrees = periodLabelAngle,
    color = dayColor,
    periodLabel = periodLabel,
    endingEdgeLabel = endingEdgeLabel,
    sunriseTimeLabel = change?.today?.sunrise?.timeLabel(using24HFormat).orEmpty(),
    sunsetTimeLabel = change?.today?.sunset?.timeLabel(using24HFormat).orEmpty(),
    sunriseDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.sunrise,
          todayTimestamp = change?.today?.sunrise
        )
      } else {
        ""
      },
    sunsetDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.sunset,
          todayTimestamp = change?.today?.sunset
        )
      } else {
        ""
      }
  )

private fun civilTwilightChartSegment(
  change: LocationSunriseSunsetChange?,
  sweepAngleDegrees: Float,
  periodLabel: String,
  endingEdgeLabel: String,
  using24HFormat: Boolean,
  showDiffLabels: Boolean,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = 0f,
    periodLabelAngleDegrees = 3f,
    color = civilTwilightColor,
    periodLabel = periodLabel,
    endingEdgeLabel = endingEdgeLabel,
    sunriseTimeLabel = change?.today?.civilTwilightBegin?.timeLabel(using24HFormat).orEmpty(),
    sunsetTimeLabel = change?.today?.civilTwilightEnd?.timeLabel(using24HFormat).orEmpty(),
    sunriseDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.civilTwilightBegin,
          todayTimestamp = change?.today?.civilTwilightBegin
        )
      } else {
        ""
      },
    sunsetDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.civilTwilightEnd,
          todayTimestamp = change?.today?.civilTwilightEnd
        )
      } else {
        ""
      }
  )

private fun nauticalTwilightChartSegment(
  change: LocationSunriseSunsetChange?,
  sweepAngleDegrees: Float,
  periodLabel: String,
  endingEdgeLabel: String,
  using24HFormat: Boolean,
  showDiffLabels: Boolean,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = 6f,
    periodLabelAngleDegrees = 9f,
    color = nauticalTwilightColor,
    periodLabel = periodLabel,
    endingEdgeLabel = endingEdgeLabel,
    sunriseTimeLabel = change?.today?.nauticalTwilightBegin?.timeLabel(using24HFormat).orEmpty(),
    sunsetTimeLabel = change?.today?.nauticalTwilightEnd?.timeLabel(using24HFormat).orEmpty(),
    sunriseDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.nauticalTwilightBegin,
          todayTimestamp = change?.today?.nauticalTwilightBegin
        )
      } else {
        ""
      },
    sunsetDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.nauticalTwilightEnd,
          todayTimestamp = change?.today?.nauticalTwilightEnd
        )
      } else {
        ""
      }
  )

private fun astronomicalTwilightChartSegment(
  change: LocationSunriseSunsetChange?,
  sweepAngleDegrees: Float,
  periodLabel: String,
  endingEdgeLabel: String,
  using24HFormat: Boolean,
  showDiffLabels: Boolean,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = 12f,
    periodLabelAngleDegrees = 15f,
    color = astronomicalTwilightColor,
    periodLabel = periodLabel,
    endingEdgeLabel = endingEdgeLabel,
    sunriseTimeLabel =
      change?.today?.astronomicalTwilightBegin?.timeLabel(using24HFormat).orEmpty(),
    sunsetTimeLabel = change?.today?.astronomicalTwilightEnd?.timeLabel(using24HFormat).orEmpty(),
    sunriseDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.astronomicalTwilightBegin,
          todayTimestamp = change?.today?.astronomicalTwilightBegin
        )
      } else {
        ""
      },
    sunsetDiffLabel =
      if (showDiffLabels) {
        timestampDiffLabel(
          yesterdayTimestamp = change?.yesterday?.astronomicalTwilightEnd,
          todayTimestamp = change?.today?.astronomicalTwilightEnd
        )
      } else {
        ""
      }
  )

private fun nightChartSegment(
  sweepAngleDegrees: Float,
  periodLabelAngle: Float,
  periodLabel: String
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = 18f,
    periodLabelAngleDegrees = periodLabelAngle,
    color = nightColor,
    periodLabel = periodLabel
  )

private fun goldenHourChartSegment(
  change: LocationSunriseSunsetChange?,
  sweepAngleDegrees: Float,
  periodLabel: String,
  endingEdgeLabel: String,
  using24HFormat: Boolean
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = -6f,
    periodLabelAngleDegrees = -1f,
    color = goldenHourColor,
    periodLabel = periodLabel,
    endingEdgeLabel = endingEdgeLabel,
    sunriseTimeLabel = change?.today?.goldenHourBelowMorning?.timeLabel(using24HFormat).orEmpty(),
    sunsetTimeLabel = change?.today?.goldenHourBelowEvening?.timeLabel(using24HFormat).orEmpty(),
    sunriseDiffLabel = "",
    sunsetDiffLabel = ""
  )

private fun blueHourChartSegment(
  change: LocationSunriseSunsetChange?,
  sweepAngleDegrees: Float,
  periodLabel: String,
  endingEdgeLabel: String,
  using24HFormat: Boolean
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    endingEdgeAngleDegrees = 4f,
    periodLabelAngleDegrees = 5f,
    color = blueHourColor,
    periodLabel = periodLabel,
    endingEdgeLabel = endingEdgeLabel,
    sunriseTimeLabel = change?.today?.blueHourBegin?.timeLabel(using24HFormat).orEmpty(),
    sunsetTimeLabel = change?.today?.blueHourEnd?.timeLabel(using24HFormat).orEmpty(),
    sunriseDiffLabel = "",
    sunsetDiffLabel = ""
  )

private fun timestampDiffLabel(
  yesterdayTimestamp: LocalDateTime?,
  todayTimestamp: LocalDateTime?
): String =
  if (yesterdayTimestamp != null && todayTimestamp != null) {
    timeDifferenceLabel(yesterdayTimestamp.toLocalTime(), todayTimestamp.toLocalTime())
  } else {
    ""
  }
