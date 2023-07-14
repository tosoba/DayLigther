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
import androidx.compose.ui.text.TextLayoutResult
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
  val segmentEdges =
    when (chartMode) {
      DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
        dayNightCycleChartSegmentEdges(change = changeValue.dataOrNull())
      }
      DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
        goldenBlueHourChartSegmentEdges(change = changeValue.dataOrNull())
      }
    }
  val segmentEdgeLabels =
    when (chartMode) {
      DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
        dayNightCycleChartSegmentEdgeAndTimeDiffLabels(
          change = changeValue.dataOrNull(),
          dayMode = dayMode
        )
      }
      DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
        goldenBlueHourChartSegmentEdgeAndTimeDiffLabels(
          change = changeValue.dataOrNull(),
          dayMode = dayMode
        )
      }
    }

  val textMeasurer = rememberTextMeasurer()
  val labelTextStyle = MaterialTheme.typography.labelSmall
  val textColor = MaterialTheme.colorScheme.onBackground

  val orientation = LocalConfiguration.current.orientation
  val dayLabel = stringResource(R.string.day)
  val horizonLabel = stringResource(R.string.horizon)
  val nowLineColor = colorResource(R.color.now_line)

  Canvas(modifier = modifier) {
    if (changeValue !is Ready) {
      drawChartSegments(chartSegments = chartSegments, orientation = orientation)
      return@Canvas
    }

    segmentEdges.forEach { edge -> drawSegmentEdge(edge = edge, orientation = orientation) }
    drawChartSegments(chartSegments = chartSegments, orientation = orientation)

    drawPeriodLabels(
      chartSegments = chartSegments,
      textMeasurer = textMeasurer,
      textStyle = labelTextStyle,
      dayLabel = dayLabel,
      orientation = orientation
    )

    segmentEdgeLabels.forEach { edgeLabels ->
      drawEndingEdgeAndTimeDiffLabels(
        edgeLabels = edgeLabels,
        textMeasurer = textMeasurer,
        textStyle = labelTextStyle.copy(color = textColor),
        dayMode = dayMode,
        orientation = orientation
      )
    }

    if (changeValue.data.today.sunrise != null && changeValue.data.today.sunset != null) {
      drawHorizonLabel(
        textStyle = labelTextStyle.copy(textAlign = TextAlign.Right, color = textColor),
        textMeasurer = textMeasurer,
        horizonLabel = horizonLabel,
        orientation = orientation
      )
    }

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
      chartMode = chartMode,
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

private data class DayChartSegmentEdge(
  val angleDegrees: Float,
  val lineRadiusMultiplier: Float,
  val color: Color,
  val strokeWidth: Float = 4f,
  val pathEffect: PathEffect? = null,
)

@Composable
private fun dayNightCycleChartSegmentEdges(
  change: LocationSunriseSunsetChange?,
): List<DayChartSegmentEdge> {
  val orientation = LocalConfiguration.current.orientation
  val lineRadiusMultiplier =
    if (orientation == Configuration.ORIENTATION_PORTRAIT) portraitLineRadiusMultiplier
    else landscapeLineRadiusMultiplier
  val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

  return remember(change) {
    buildList {
      if (change?.today?.sunrise != null && change.today.sunset != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 0f,
            lineRadiusMultiplier = 10f,
            color = civilTwilightColor,
          )
        )
      }
      if (change?.today?.morning6Below != null && change.today.evening6Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 6f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = nauticalTwilightColor,
            pathEffect = dashPathEffect,
          )
        )
      }
      if (change?.today?.morning12Below != null && change.today.evening12Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 12f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = astronomicalTwilightColor,
            pathEffect = dashPathEffect,
          )
        )
      }
      if (change?.today?.morning18Below != null && change.today.evening18Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 18f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = nightColor,
            pathEffect = dashPathEffect,
          )
        )
      }
    }
  }
}

@Composable
private fun goldenBlueHourChartSegmentEdges(
  change: LocationSunriseSunsetChange?,
): List<DayChartSegmentEdge> {
  val orientation = LocalConfiguration.current.orientation
  val lineRadiusMultiplier =
    if (orientation == Configuration.ORIENTATION_PORTRAIT) portraitLineRadiusMultiplier
    else landscapeLineRadiusMultiplier
  val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

  return remember(change) {
    buildList {
      if (change?.today?.morning6Above != null && change.today.evening6Above != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = -6f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = goldenHourColor,
            pathEffect = dashPathEffect,
          )
        )
      }
      if (change?.today?.sunrise != null && change.today.sunset != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 0f,
            lineRadiusMultiplier = 10f,
            color = civilTwilightColor,
          )
        )
      }
      if (change?.today?.morning4Below != null && change.today.evening4Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 4f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = blueHourColor,
            pathEffect = dashPathEffect,
          )
        )
      }
      if (change?.today?.morning6Below != null && change.today.evening6Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 6f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = nauticalTwilightColor,
            pathEffect = dashPathEffect,
          )
        )
      }
      if (change?.today?.morning12Below != null && change.today.evening12Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 12f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = astronomicalTwilightColor,
            pathEffect = dashPathEffect,
          )
        )
      }
      if (change?.today?.morning18Below != null && change.today.evening18Below != null) {
        add(
          DayChartSegmentEdge(
            angleDegrees = 18f,
            lineRadiusMultiplier = lineRadiusMultiplier,
            color = nightColor,
            pathEffect = dashPathEffect,
          )
        )
      }
    }
  }
}

private fun DrawScope.drawSegmentEdge(edge: DayChartSegmentEdge, orientation: Int) {
  clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
    val chartCenter = chartCenter(orientation)
    drawLine(
      color = edge.color,
      start = chartCenter,
      end =
        Offset(
          x =
            chartCenter.x +
              chartRadius * edge.lineRadiusMultiplier * cos(edge.angleDegrees.radians),
          y =
            chartCenter.y +
              chartRadius * edge.lineRadiusMultiplier * sin(edge.angleDegrees.radians) +
              edge.strokeWidth
        ),
      strokeWidth = edge.strokeWidth,
      pathEffect = edge.pathEffect
    )
  }
}

private data class DayChartSegmentEdgeLabels(
  val edgeAngleDegrees: Float,
  val extraOffset: (TextLayoutResult) -> Offset = { Offset.Zero },
  val endingEdgeLabel: String = "",
  val sunriseTimeLabel: String = "",
  val sunsetTimeLabel: String = "",
  val sunriseDiffLabel: String = "",
  val sunsetDiffLabel: String = "",
)

@Composable
private fun dayNightCycleChartSegmentEdgeAndTimeDiffLabels(
  change: LocationSunriseSunsetChange?,
  dayMode: DayMode
): List<DayChartSegmentEdgeLabels> {
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

  val sunriseLabel = stringResource(R.string.sunrise)
  val sunsetLabel = stringResource(R.string.sunset)
  val sixDegreesBelowLabel = stringResource(R.string.six_degrees)
  val twelveDegreesBelowLabel = stringResource(R.string.twelve_degrees)
  val eighteenDegreesBelowLabel = stringResource(R.string.eighteen_degrees)

  return remember(change, dayMode) {
    buildList {
      if (change?.today?.sunrise != null && change.today.sunset != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 0f,
            endingEdgeLabel =
              when (dayMode) {
                DayMode.SUNRISE -> sunriseLabel
                DayMode.SUNSET -> sunsetLabel
              },
            sunriseTimeLabel = change.today.sunrise?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.sunset?.timeLabel(using24HFormat).orEmpty(),
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.sunrise,
                todayTimestamp = change.today.sunrise
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.sunset,
                todayTimestamp = change.today.sunset
              )
          )
        )
      }
      if (change?.today?.morning6Below != null && change.today.evening6Below != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 6f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 4f) },
            endingEdgeLabel = sixDegreesBelowLabel,
            sunriseTimeLabel = change.today.morning6Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening6Below?.timeLabel(using24HFormat).orEmpty(),
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.morning6Below,
                todayTimestamp = change.today.morning6Below
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.evening6Below,
                todayTimestamp = change.today.evening6Below
              )
          )
        )
      }
      if (change?.today?.morning12Below != null && change.today.evening12Below != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 12f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 4f) },
            endingEdgeLabel = twelveDegreesBelowLabel,
            sunriseTimeLabel = change.today.morning12Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening12Below?.timeLabel(using24HFormat).orEmpty(),
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.morning12Below,
                todayTimestamp = change.today.morning12Below
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.evening12Below,
                todayTimestamp = change.today.evening12Below
              )
          )
        )
      }
      if (change?.today?.morning18Below != null && change.today.evening18Below != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 18f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 4f) },
            endingEdgeLabel = eighteenDegreesBelowLabel,
            sunriseTimeLabel = change.today.morning18Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening18Below?.timeLabel(using24HFormat).orEmpty(),
            sunriseDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.morning18Below,
                todayTimestamp = change.today.morning18Below
              ),
            sunsetDiffLabel =
              timestampDiffLabel(
                yesterdayTimestamp = change.yesterday.evening18Below,
                todayTimestamp = change.today.evening18Below
              )
          )
        )
      }
    }
  }
}

@Composable
private fun goldenBlueHourChartSegmentEdgeAndTimeDiffLabels(
  change: LocationSunriseSunsetChange?,
  dayMode: DayMode
): List<DayChartSegmentEdgeLabels> {
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

  val sunriseLabel = stringResource(R.string.sunrise)
  val sunsetLabel = stringResource(R.string.sunset)
  val fourDegreesLabel = stringResource(R.string.four_degrees)
  val sixDegreesLabel = stringResource(R.string.six_degrees)
  val twelveDegreesLabel = stringResource(R.string.twelve_degrees)
  val eighteenDegreesLabel = stringResource(R.string.eighteen_degrees)

  return remember(change, dayMode) {
    buildList {
      if (change?.today?.morning6Above != null && change.today.evening6Above != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = -6f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 2f) },
            endingEdgeLabel = sixDegreesLabel,
            sunriseTimeLabel = change.today.morning6Above?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening6Above?.timeLabel(using24HFormat).orEmpty(),
          )
        )
      }
      if (change?.today?.sunrise != null && change.today.sunset != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 0f,
            endingEdgeLabel =
              when (dayMode) {
                DayMode.SUNRISE -> sunriseLabel
                DayMode.SUNSET -> sunsetLabel
              },
            sunriseTimeLabel = change.today.sunrise?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.sunset?.timeLabel(using24HFormat).orEmpty(),
          )
        )
      }
      if (change?.today?.morning4Below != null && change.today.evening6Above != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 4f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 2f) },
            endingEdgeLabel = fourDegreesLabel,
            sunriseTimeLabel = change.today.morning4Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening6Above?.timeLabel(using24HFormat).orEmpty(),
          )
        )
      }

      if (change?.today?.morning6Below != null && change.today.evening6Below != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 6f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 4f) },
            endingEdgeLabel = sixDegreesLabel,
            sunriseTimeLabel = change.today.morning6Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening6Below?.timeLabel(using24HFormat).orEmpty(),
          )
        )
      }
      if (change?.today?.morning12Below != null && change.today.evening12Below != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 12f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 4f) },
            endingEdgeLabel = twelveDegreesLabel,
            sunriseTimeLabel = change.today.morning12Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening12Below?.timeLabel(using24HFormat).orEmpty(),
          )
        )
      }
      if (change?.today?.morning18Below != null && change.today.evening18Below != null) {
        add(
          DayChartSegmentEdgeLabels(
            edgeAngleDegrees = 18f,
            extraOffset = { textLayoutResult -> Offset(0f, -textLayoutResult.size.height / 4f) },
            endingEdgeLabel = eighteenDegreesLabel,
            sunriseTimeLabel = change.today.morning18Below?.timeLabel(using24HFormat).orEmpty(),
            sunsetTimeLabel = change.today.evening18Below?.timeLabel(using24HFormat).orEmpty(),
          )
        )
      }
    }
  }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawEndingEdgeAndTimeDiffLabels(
  edgeLabels: DayChartSegmentEdgeLabels,
  textMeasurer: TextMeasurer,
  textStyle: TextStyle,
  dayMode: DayMode,
  orientation: Int
) {
  val chartCenter = chartCenter(orientation)
  val textRadiusMultiplier = chartTextRadiusMultiplier(orientation)

  val endingEdgeLabel = AnnotatedString(edgeLabels.endingEdgeLabel)
  val endingEdgeTextStyle = textStyle.copy(textAlign = TextAlign.Left)
  val endingEdgeLabelLayoutResult =
    textMeasurer.measure(
      text = endingEdgeLabel,
      style = endingEdgeTextStyle,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1
    )
  val endingEdgeLabelExtraOffset = edgeLabels.extraOffset(endingEdgeLabelLayoutResult)
  val endingEdgeLabelTopLeft =
    Offset(
      x =
        chartCenter.x +
          chartRadius * textRadiusMultiplier * cos(edgeLabels.edgeAngleDegrees.radians) +
          chartTextPaddingPx +
          endingEdgeLabelExtraOffset.x,
      y =
        chartCenter.y +
          chartRadius * textRadiusMultiplier * sin(edgeLabels.edgeAngleDegrees.radians) +
          endingEdgeLabelExtraOffset.y
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
      buildTimeAndDiffLabel(edgeLabels = edgeLabels, dayMode = dayMode, orientation = orientation)
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
  val timeAndDiffLabelExtraOffset = edgeLabels.extraOffset(timeLayoutResult)
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
        chartCenter.y +
          chartRadius * textRadiusMultiplier * sin(edgeLabels.edgeAngleDegrees.radians) +
          timeAndDiffLabelExtraOffset.y
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
  edgeLabels: DayChartSegmentEdgeLabels,
  dayMode: DayMode,
  orientation: Int
): String {
  val timeLabel =
    edgeLabels.run { if (dayMode == DayMode.SUNRISE) sunriseTimeLabel else sunsetTimeLabel }
  if (timeLabel.isBlank()) return ""
  val diffLabel =
    edgeLabels.run { if (dayMode == DayMode.SUNRISE) sunriseDiffLabel else sunsetDiffLabel }
  if (diffLabel.isBlank()) return timeLabel
  return buildString {
    append(timeLabel)
    append(if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " ")
    append(diffLabel)
  }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawHorizonLabel(
  textStyle: TextStyle,
  textMeasurer: TextMeasurer,
  horizonLabel: String,
  orientation: Int
) {
  val horizonLayoutResult =
    textMeasurer.measure(
      text = AnnotatedString(horizonLabel),
      style = textStyle,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1
    )
  drawText(
    textMeasurer = textMeasurer,
    text = horizonLabel,
    topLeft =
      Offset(
        x = size.width - horizonLayoutResult.size.width - chartTextPaddingPx,
        y = chartCenter(orientation).y - horizonLayoutResult.size.height - chartTextPaddingPx / 2f
      ),
    style = textStyle,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )
}

private fun DrawScope.drawNowLine(
  today: SunriseSunset,
  location: Location,
  now: LocalTime,
  dayMode: DayMode,
  chartMode: DayPeriodChartMode,
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
        chartMode = chartMode,
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
  chartMode: DayPeriodChartMode,
  canvasHeight: Float,
  appBarHeight: Float,
  chartRadius: Float,
): Float {
  val dayPeriod =
    sunriseSunset.currentPeriodIn(
      location = location,
      useGoldenBlueHour = chartMode == DayPeriodChartMode.GOLDEN_BLUE_HOUR
    )

  val startAngle =
    sunriseSunset.dayPeriodStartAngleRadians(
      dayPeriod = dayPeriod,
      dayMode = dayMode,
      chartMode = chartMode,
      canvasHeight = canvasHeight,
      appBarHeight = appBarHeight,
      chartRadius = chartRadius
    )
  val endAngle =
    sunriseSunset.dayPeriodEndAngleRadians(
      dayPeriod = dayPeriod,
      dayMode = dayMode,
      chartMode = chartMode,
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
  chartMode: DayPeriodChartMode,
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
        DayMode.SUNSET -> evening18Below?.let { 18f.radians } ?: dayStart
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning18Below?.let { 18f.radians } ?: nightStart
        DayMode.SUNSET -> evening12Below?.let { 12f.radians } ?: dayStart
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning12Below?.let { 12f.radians } ?: nightStart
        DayMode.SUNSET -> evening6Below?.let { 6f.radians } ?: dayStart
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Below?.let { 6f.radians } ?: nightStart
        DayMode.SUNSET -> sunset?.let { 0f.radians } ?: dayStart
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> {
          when (chartMode) {
            DayPeriodChartMode.DAY_NIGHT_CYCLE -> sunset?.let { 0f.radians }
            DayPeriodChartMode.GOLDEN_BLUE_HOUR -> morning6Above?.let { (-6f).radians }
          }
            ?: nightStart
        }
        DayMode.SUNSET -> {
          dayStart
        }
      }
    }
    DayPeriod.GOLDEN_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning4Below?.let { 4f.radians } ?: nightStart
        DayMode.SUNSET -> evening6Above?.let { (-6f).radians } ?: dayStart
      }
    }
    DayPeriod.BLUE_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Below?.let { 6f.radians } ?: nightStart
        DayMode.SUNSET -> evening4Below?.let { 4f.radians } ?: dayStart
      }
    }
  }
}

private fun SunriseSunset.dayPeriodEndAngleRadians(
  dayPeriod: DayPeriod,
  dayMode: DayMode,
  chartMode: DayPeriodChartMode,
  canvasHeight: Float,
  appBarHeight: Float,
  chartRadius: Float,
): Float {
  val nightEnd = asin(canvasHeight / (2f * chartRadius))
  val dayEnd = -asin((canvasHeight - appBarHeight) / (2f * chartRadius))
  return when (dayPeriod) {
    DayPeriod.NIGHT -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning18Below?.let { 18f.radians } ?: dayEnd
        DayMode.SUNSET -> nightEnd
      }
    }
    DayPeriod.ASTRONOMICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning12Below?.let { 12f.radians } ?: dayEnd
        DayMode.SUNSET -> evening18Below?.let { 18f.radians } ?: nightEnd
      }
    }
    DayPeriod.NAUTICAL -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Below?.let { 6f.radians } ?: dayEnd
        DayMode.SUNSET -> evening12Below?.let { 12f.radians } ?: nightEnd
      }
    }
    DayPeriod.CIVIL -> {
      when (dayMode) {
        DayMode.SUNRISE -> sunrise?.let { 0f.radians } ?: dayEnd
        DayMode.SUNSET -> evening6Below?.let { 6f.radians } ?: nightEnd
      }
    }
    DayPeriod.DAY -> {
      when (dayMode) {
        DayMode.SUNRISE -> {
          dayEnd
        }
        DayMode.SUNSET -> {
          when (chartMode) {
            DayPeriodChartMode.DAY_NIGHT_CYCLE -> sunset?.let { 0f.radians }
            DayPeriodChartMode.GOLDEN_BLUE_HOUR -> evening6Above?.let { (-6f).radians }
          }
            ?: nightEnd
        }
      }
    }
    DayPeriod.GOLDEN_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning6Above?.let { (-6f).radians } ?: dayEnd
        DayMode.SUNSET -> evening4Below?.let { 4f.radians } ?: nightEnd
      }
    }
    DayPeriod.BLUE_HOUR -> {
      when (dayMode) {
        DayMode.SUNRISE -> morning4Below?.let { 4f.radians } ?: dayEnd
        DayMode.SUNSET -> evening6Below?.let { 6f.radians } ?: nightEnd
      }
    }
  }
}

private data class DayChartSegment(
  val sweepAngleDegrees: Float,
  val periodLabelAngleDegrees: Float,
  val color: Color,
  val periodLabel: String,
)

@Composable
private fun dayNightCycleChartSegments(
  change: LocationSunriseSunsetChange?,
  dayMode: DayMode,
): List<DayChartSegment> {
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

  val dayLabel = stringResource(R.string.day)
  val civilTwilightLabel = stringResource(R.string.civil_twilight)
  val nauticalTwilightLabel = stringResource(R.string.nautical_twilight)
  val astronomicalTwilightLabel = stringResource(R.string.astronomical_twilight)
  val nightLabel = stringResource(R.string.night)

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
            sweepAngleDegrees = 90f + accumulatedSweepAngle,
            periodLabelAngle = 0f,
            periodLabel = dayLabel,
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 90f
      }

      if (
        change == null ||
          (change.today.sunrise != null && change.today.sunset != null) ||
          (change.today.morning6Below != null && change.today.evening6Below != null)
      ) {
        add(
          civilTwilightChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = civilTwilightLabel,
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.morning6Below != null && change.today.evening6Below != null) ||
          (change.today.morning12Below != null && change.today.evening12Below != null)
      ) {
        add(
          nauticalTwilightChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = nauticalTwilightLabel,
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.morning12Below != null && change.today.evening12Below != null) ||
          (change.today.morning18Below != null && change.today.evening18Below != null)
      ) {
        add(
          astronomicalTwilightChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = astronomicalTwilightLabel,
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.morning18Below != null && change.today.evening18Below != null) ||
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
  val using24HFormat = DateFormat.is24HourFormat(LocalContext.current)

  val dayLabel = stringResource(R.string.day)
  val goldenHourLabel = stringResource(R.string.golden_hour)
  val blueHourLabel = stringResource(R.string.blue_hour)
  val nauticalTwilightLabel = stringResource(R.string.nautical_twilight)
  val astronomicalTwilightLabel = stringResource(R.string.astronomical_twilight)
  val nightLabel = stringResource(R.string.night)

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
            sweepAngleDegrees = 84f + accumulatedSweepAngle,
            periodLabelAngle =
              if (change?.today?.isPolarDayAtLocation(change.location) == true) 0f else -9f,
            periodLabel = dayLabel
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 84f
      }

      if (
        change == null ||
          (change.today.morning6Above != null && change.today.evening6Above != null) ||
          (change.today.morning4Below != null && change.today.evening4Below != null)
      ) {
        add(
          goldenHourChartSegment(
            sweepAngleDegrees = 10f + accumulatedSweepAngle,
            periodLabel = goldenHourLabel,
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 10f
      }

      if (
        change == null ||
          (change.today.morning4Below != null && change.today.evening4Below != null) ||
          (change.today.morning6Below != null && change.today.evening6Below != null)
      ) {
        add(
          blueHourChartSegment(
            sweepAngleDegrees = 2f + accumulatedSweepAngle,
            periodLabel = blueHourLabel
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 2f
      }

      if (
        change == null ||
          (change.today.morning6Below != null && change.today.evening6Below != null) ||
          (change.today.morning12Below != null && change.today.evening12Below != null)
      ) {
        add(
          nauticalTwilightChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = nauticalTwilightLabel
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.morning12Below != null && change.today.evening12Below != null) ||
          (change.today.morning18Below != null && change.today.evening18Below != null)
      ) {
        add(
          astronomicalTwilightChartSegment(
            sweepAngleDegrees = 6f + accumulatedSweepAngle,
            periodLabel = astronomicalTwilightLabel
          )
        )
        accumulatedSweepAngle = 0f
      } else {
        accumulatedSweepAngle += 6f
      }

      if (
        change == null ||
          (change.today.morning18Below != null && change.today.evening18Below != null) ||
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
  sweepAngleDegrees: Float,
  periodLabelAngle: Float,
  periodLabel: String,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = periodLabelAngle,
    color = dayColor,
    periodLabel = periodLabel,
  )

private fun civilTwilightChartSegment(
  sweepAngleDegrees: Float,
  periodLabel: String,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = 3f,
    color = civilTwilightColor,
    periodLabel = periodLabel,
  )

private fun nauticalTwilightChartSegment(
  sweepAngleDegrees: Float,
  periodLabel: String,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = 9f,
    color = nauticalTwilightColor,
    periodLabel = periodLabel,
  )

private fun astronomicalTwilightChartSegment(
  sweepAngleDegrees: Float,
  periodLabel: String,
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = 15f,
    color = astronomicalTwilightColor,
    periodLabel = periodLabel,
  )

private fun nightChartSegment(
  sweepAngleDegrees: Float,
  periodLabelAngle: Float,
  periodLabel: String
): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = periodLabelAngle,
    color = nightColor,
    periodLabel = periodLabel
  )

private fun goldenHourChartSegment(sweepAngleDegrees: Float, periodLabel: String): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = -1f,
    color = goldenHourColor,
    periodLabel = periodLabel,
  )

private fun blueHourChartSegment(sweepAngleDegrees: Float, periodLabel: String): DayChartSegment =
  DayChartSegment(
    sweepAngleDegrees = sweepAngleDegrees,
    periodLabelAngleDegrees = 5f,
    color = blueHourColor,
    periodLabel = periodLabel,
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
