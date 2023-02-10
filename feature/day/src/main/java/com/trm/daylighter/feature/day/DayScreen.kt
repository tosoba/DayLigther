package com.trm.daylighter.feature.day

import android.content.res.Configuration
import android.graphics.Typeface
import android.text.format.DateFormat
import android.widget.TextClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.trm.daylighter.core.common.util.ext.radians
import com.trm.daylighter.core.common.util.ext.timeLabel
import com.trm.daylighter.core.common.util.takeIfInstance
import com.trm.daylighter.domain.model.*
import java.lang.Float.max
import kotlin.math.cos
import kotlin.math.sin

const val dayRoute = "day_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DayRoute(
  onDrawerMenuClick: () -> Unit,
  onAddLocation: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locationSunriseSunsetChange =
    viewModel.currentLocationSunriseSunsetChange.collectAsStateWithLifecycle(
      initialValue = LoadingFirst
    )
  val locationsCount = viewModel.locationCountFlow.collectAsStateWithLifecycle(initialValue = 0)
  DayScreen(
    locationSunriseSunsetChange = locationSunriseSunsetChange.value,
    locationsCount = locationsCount.value,
    currentLocationIndex = viewModel.currentLocationIndex,
    onDrawerMenuClick = onDrawerMenuClick,
    onChangeLocationIndex = viewModel::changeLocation,
    onAddLocationClick = onAddLocation,
    onRetryClick = viewModel::retry,
    modifier = modifier
  )
}

private data class DayChartSegment(
  val sweepAngleDegrees: Float,
  val color: Color,
  val periodLabel: String,
  val sunriseEndingEdgeLabel: String = "",
  val sunsetEndingEdgeLabel: String = "",
  val sunriseTimeLabel: (() -> String)? = null,
  val sunsetTimeLabel: (() -> String)? = null,
)

@Composable
private fun DayScreen(
  locationSunriseSunsetChange: Loadable<LocationSunriseSunsetChange>,
  locationsCount: Int,
  currentLocationIndex: Int,
  onDrawerMenuClick: () -> Unit,
  onChangeLocationIndex: (Int) -> Unit,
  onAddLocationClick: () -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var dayMode by rememberSaveable { mutableStateOf(DayMode.SUNRISE) }

  ConstraintLayout(modifier = modifier) {
    when (locationSunriseSunsetChange) {
      is Empty -> {
        val (drawerMenuButton, addLocationButton) = createRefs()
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier =
            Modifier.constrainAs(drawerMenuButton) {
              start.linkTo(parent.start, 16.dp)
              top.linkTo(parent.top, 16.dp)
            }
        )
        Button(
          onClick = onAddLocationClick,
          modifier =
            Modifier.constrainAs(addLocationButton) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        ) {
          Text(text = "Add location")
        }
      }
      is Ready -> {
        SunriseSunset(
          locationSunriseSunsetChange = locationSunriseSunsetChange.data,
          dayMode = dayMode,
          locationsCount = locationsCount,
          currentLocationIndex = currentLocationIndex,
          onChangeLocationIndex = onChangeLocationIndex,
          onDayModeNavClick = { dayMode = it },
          onDrawerMenuClick = onDrawerMenuClick
        )
      }
      is Failed -> {
        val (drawerMenuButton, retryButton) = createRefs()
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier =
            Modifier.constrainAs(drawerMenuButton) {
              start.linkTo(parent.start, 16.dp)
              top.linkTo(parent.top, 16.dp)
            }
        )
        Button(
          onClick = onRetryClick,
          modifier =
            Modifier.constrainAs(retryButton) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        ) {
          Text("Retry")
        }
      }
      is Loading -> {
        val (drawerMenuButton, loadingIndicator) = createRefs()
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier =
            Modifier.constrainAs(drawerMenuButton) {
              start.linkTo(parent.start, 16.dp)
              top.linkTo(parent.top, 16.dp)
            }
        )

        CircularProgressIndicator(
          modifier =
            Modifier.constrainAs(loadingIndicator) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        )
      }
    }
  }
}

@Composable
private fun DrawerMenuButton(onDrawerMenuClick: () -> Unit, modifier: Modifier = Modifier) {
  SmallFloatingActionButton(onClick = onDrawerMenuClick, modifier = modifier) {
    Icon(imageVector = Icons.Filled.Menu, contentDescription = "drawer_menu")
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ConstraintLayoutScope.SunriseSunset(
  locationSunriseSunsetChange: LocationSunriseSunsetChange,
  dayMode: DayMode,
  locationsCount: Int,
  currentLocationIndex: Int,
  onChangeLocationIndex: (Int) -> Unit,
  onDayModeNavClick: (DayMode) -> Unit,
  onDrawerMenuClick: () -> Unit,
) {
  val (drawerMenuButton, pagerBox, navigation, dayTimeCard) = createRefs()
  val orientation = LocalConfiguration.current.orientation

  val pagerState = rememberPagerState(initialPage = currentLocationIndex)
  LaunchedEffect(pagerState) {
    snapshotFlow(pagerState::currentPage).collect(onChangeLocationIndex)
  }

  Box(
    modifier =
      Modifier.constrainAs(pagerBox) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          linkTo(parent.start, parent.end)
          linkTo(parent.top, navigation.top)
        } else {
          linkTo(navigation.end, parent.end)
          linkTo(parent.top, parent.bottom)
        }
        height = Dimension.fillToConstraints
        width = Dimension.fillToConstraints
      }
  ) {
    HorizontalPager(count = locationsCount, state = pagerState, modifier = Modifier.fillMaxSize()) {
      SunriseSunsetChart(
        locationSunriseSunsetChange = locationSunriseSunsetChange,
        dayMode = dayMode,
        modifier = Modifier.fillMaxSize()
      )
    }

    HorizontalPagerIndicator(
      pagerState = pagerState,
      modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
    )
  }

  Card(
    modifier =
      Modifier.constrainAs(dayTimeCard) {
        top.linkTo(parent.top, 16.dp)
        start.linkTo(parent.start, 16.dp)
        end.linkTo(parent.end, 16.dp)
      }
  ) {
    val labelMediumStyle = MaterialTheme.typography.labelMedium
    val resolver = LocalFontFamilyResolver.current
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    AndroidView(
      factory = { context ->
        TextClock(context).apply {
          format24Hour = "hh:mm:ss"
          format12Hour = "hh:mm:ss a"
          resolver
            .resolve(
              fontFamily = labelMediumStyle.fontFamily,
              fontWeight = labelMediumStyle.fontWeight ?: FontWeight.Normal,
              fontStyle = labelMediumStyle.fontStyle ?: FontStyle.Normal,
              fontSynthesis = labelMediumStyle.fontSynthesis ?: FontSynthesis.All,
            )
            .value
            .takeIfInstance<Typeface>()
            ?.let { typeface = it }
          textSize = 18f
          setTextColor(textColor)
        }
      },
      update = { clockView ->
        clockView.timeZone = locationSunriseSunsetChange.today.sunrise.zone.id
      },
      modifier = Modifier.padding(16.dp)
    )
  }

  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
    DrawerMenuButton(
      onDrawerMenuClick = onDrawerMenuClick,
      modifier =
        Modifier.constrainAs(drawerMenuButton) {
          start.linkTo(parent.start, 16.dp)
          top.linkTo(parent.top, 16.dp)
        }
    )

    SunriseSunsetNavigationBar(
      modifier =
        Modifier.constrainAs(navigation) {
          linkTo(pagerBox.bottom, parent.bottom)
          linkTo(parent.start, parent.end)
        },
      dayMode = dayMode,
      onDayModeChange = onDayModeNavClick
    )
  } else {
    SunriseSunsetNavigationRail(
      modifier =
        Modifier.constrainAs(navigation) {
          linkTo(parent.start, pagerBox.start)
          linkTo(parent.top, parent.bottom)
        },
      onDrawerMenuClick = onDrawerMenuClick,
      dayMode = dayMode,
      onDayModeChange = onDayModeNavClick
    )
  }
}

@Composable
private fun SunriseSunsetNavigationBar(
  modifier: Modifier = Modifier,
  dayMode: DayMode,
  onDayModeChange: (DayMode) -> Unit,
) {
  NavigationBar(modifier = modifier) {
    NavigationBarItem(
      selected = dayMode == DayMode.SUNRISE,
      onClick = { onDayModeChange(DayMode.SUNRISE) },
      icon = {
        Icon(painter = painterResource(R.drawable.sunrise), contentDescription = "sunrise")
      },
      label = { Text(text = stringResource(R.string.sunrise)) }
    )
    NavigationBarItem(
      selected = dayMode == DayMode.SUNSET,
      onClick = { onDayModeChange(DayMode.SUNSET) },
      icon = { Icon(painter = painterResource(R.drawable.sunset), contentDescription = "sunset") },
      label = { Text(text = stringResource(R.string.sunset)) }
    )
  }
}

@Composable
private fun SunriseSunsetNavigationRail(
  dayMode: DayMode,
  onDayModeChange: (DayMode) -> Unit,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationRail(
    header = {
      DrawerMenuButton(
        onDrawerMenuClick = onDrawerMenuClick,
        modifier = Modifier.padding(top = 8.dp)
      )
    },
    modifier = modifier
  ) {
    Spacer(modifier = Modifier.weight(1f))
    NavigationRailItem(
      selected = dayMode == DayMode.SUNRISE,
      onClick = { onDayModeChange(DayMode.SUNRISE) },
      icon = {
        Icon(painter = painterResource(R.drawable.sunrise), contentDescription = "sunrise")
      },
      label = { Text(text = stringResource(R.string.sunrise)) }
    )
    NavigationRailItem(
      selected = dayMode == DayMode.SUNSET,
      onClick = { onDayModeChange(DayMode.SUNSET) },
      icon = { Icon(painter = painterResource(R.drawable.sunset), contentDescription = "sunset") },
      label = { Text(text = stringResource(R.string.sunset)) }
    )
    Spacer(modifier = Modifier.weight(1f))
  }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun SunriseSunsetChart(
  locationSunriseSunsetChange: LocationSunriseSunsetChange,
  dayMode: DayMode,
  modifier: Modifier = Modifier
) {
  val (location, today, yesterday) = locationSunriseSunsetChange
  val orientation = LocalConfiguration.current.orientation

  val chartSegments =
    dayChartSegments(
      today = today,
      orientation = orientation,
      using24HFormat = DateFormat.is24HourFormat(LocalContext.current)
    )

  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall

  val sunPainter = rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.sun))

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
        drawArc(
          color = segment.color,
          startAngle = startAngle,
          sweepAngle = segment.sweepAngleDegrees,
          useCenter = true,
          topLeft = topLeftOffset,
          size = segmentSize
        )
      }
    }

    chartSegments.forEach { segment ->
      drawChartSegment(segment)
      startAngle += segment.sweepAngleDegrees
    }

    val chartRadius = segmentSize.maxDimension / 2f
    val chartCenter = Offset(topLeftOffset.x + chartRadius, size.height / 2f)
    var currentAngleDegrees = 0f
    val angleIncrementDegrees = 6f
    val textPadding = 3.dp.toPx()
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    val horizonLayoutResult = textMeasurer.measure(text = AnnotatedString("Horizon"))
    drawText(
      textMeasurer = textMeasurer,
      text = "Horizon",
      topLeft =
        Offset(
          x = size.width - horizonLayoutResult.size.width - textPadding,
          y = chartCenter.y - horizonLayoutResult.size.height - textPadding
        ),
      style = labelSmallTextStyle.copy(textAlign = TextAlign.Right),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    repeat(chartSegments.size - 1) { segmentIndex ->
      clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
        val lineRadiusMultiplier =
          when {
            segmentIndex == 0 -> 10f
            orientation == Configuration.ORIENTATION_PORTRAIT -> 1.025f
            else -> 1.1f
          }
        val strokeWidth = 2f
        drawLine(
          color = chartSegments[segmentIndex + 1].color,
          start = chartCenter,
          end =
            Offset(
              chartCenter.x + chartRadius * lineRadiusMultiplier * cos(currentAngleDegrees.radians),
              chartCenter.y +
                chartRadius * lineRadiusMultiplier * sin(currentAngleDegrees.radians) +
                strokeWidth
            ),
          strokeWidth = strokeWidth,
          pathEffect = if (segmentIndex == 0) null else dashPathEffect
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
              chartRadius * textRadiusMultiplier * cos(currentAngleDegrees.radians) +
              textPadding,
          y =
            chartCenter.y + chartRadius * textRadiusMultiplier * sin(currentAngleDegrees.radians) -
              if (segmentIndex == 0) 0f else endingEdgeLabelLayoutResult.size.height / 2f
        )
      drawText(
        textMeasurer = textMeasurer,
        text = endingEdgeLabel,
        topLeft = endingEdgeLabelTopLeft,
        style = labelSmallTextStyle.copy(textAlign = TextAlign.Left),
        maxLines = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 1,
        overflow = TextOverflow.Ellipsis,
      )

      val timeLabel =
        chartSegments[segmentIndex].run {
          requireNotNull(if (dayMode == DayMode.SUNRISE) sunriseTimeLabel else sunsetTimeLabel)
        }()
      val timeLayoutResult = textMeasurer.measure(text = AnnotatedString(timeLabel))
      val timeTopLeft =
        Offset(
          x =
            max(
              endingEdgeLabelTopLeft.x +
                endingEdgeLabelLayoutResult.size.width.toFloat() +
                textPadding,
              size.width - timeLayoutResult.size.width - textPadding
            ),
          y =
            chartCenter.y + chartRadius * textRadiusMultiplier * sin(currentAngleDegrees.radians) -
              if (segmentIndex == 0) 0f else timeLayoutResult.size.height / 2f
        )
      drawText(
        textMeasurer = textMeasurer,
        text = timeLabel,
        topLeft = timeTopLeft,
        style = labelSmallTextStyle.copy(textAlign = TextAlign.Right),
        maxLines = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 1,
        overflow = TextOverflow.Ellipsis,
      )

      currentAngleDegrees += angleIncrementDegrees
    }

    currentAngleDegrees = 0f
    chartSegments.forEachIndexed { index, segment ->
      rotate(
        degrees = (currentAngleDegrees - angleIncrementDegrees / 2f).coerceAtLeast(0f),
        pivot = chartCenter
      ) {
        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(segment.periodLabel))
        drawText(
          textMeasurer = textMeasurer,
          text = segment.periodLabel,
          topLeft =
            Offset(
              x = chartCenter.x + chartRadius - textLayoutResult.size.width - textPadding,
              y =
                if (index == 0) chartCenter.y - textLayoutResult.size.height - textPadding
                else chartCenter.y - textLayoutResult.size.height / 2f
            ),
          style =
            labelSmallTextStyle.copy(
              color = if (index == 0) Color.Black else Color.White,
              textAlign = TextAlign.Right
            ),
        )
        currentAngleDegrees += angleIncrementDegrees
      }
    }

    val sunArcTopLeft =
      Offset(
        topLeftOffset.x -
          if (orientation == Configuration.ORIENTATION_PORTRAIT) chartRadius / 8f
          else chartRadius / 2f,
        topLeftOffset.y
      )
    val sunArcCenter = Offset(sunArcTopLeft.x + chartRadius, size.height / 2f)
    val sunArcSweepAngle = if (orientation == Configuration.ORIENTATION_PORTRAIT) 10f else 20f
    drawArc(
      color = Color.Yellow,
      startAngle = 360f - sunArcSweepAngle / 2f,
      sweepAngle = sunArcSweepAngle,
      useCenter = false,
      topLeft = sunArcTopLeft,
      size = segmentSize,
      style = Stroke(width = 2f, pathEffect = dashPathEffect),
    )

    val arrowHeadCenterX = (sunArcTopLeft.x + segmentSize.maxDimension)
    val arrowHeadPath =
      buildSunArcArrowHeadPath(
        dayMode = dayMode,
        arrowHeadCenterX = arrowHeadCenterX,
        arrowHeadDimension = 10.dp.toPx()
      )
    rotate(
      degrees = if (dayMode == DayMode.SUNRISE) -sunArcSweepAngle / 2f else sunArcSweepAngle / 2f,
      pivot = sunArcCenter
    ) {
      drawPath(path = arrowHeadPath, color = Color.Yellow)
    }

    clipRect(
      left = arrowHeadCenterX - sunPainter.intrinsicSize.width / 2f,
      top = size.height / 2f - sunPainter.intrinsicSize.height / 2f,
      right = arrowHeadCenterX + sunPainter.intrinsicSize.width / 2f,
      bottom = size.height / 2f,
    ) {
      translate(
        arrowHeadCenterX - sunPainter.intrinsicSize.width / 2f,
        size.height / 2f - sunPainter.intrinsicSize.height / 2f
      ) {
        with(sunPainter) { draw(intrinsicSize) }
      }
    }
  }
}

@Composable
private fun dayChartSegments(
  today: SunriseSunset,
  orientation: Int,
  using24HFormat: Boolean
): List<DayChartSegment> {
  val sunrise = stringResource(id = R.string.sunrise)
  val sunset = stringResource(id = R.string.sunset)
  return remember(today) {
    listOf(
      DayChartSegment(
        sweepAngleDegrees = 90f,
        color = Color(0xFFB9D9E5),
        periodLabel = "Day",
        sunriseEndingEdgeLabel = sunrise,
        sunsetEndingEdgeLabel = sunset,
        sunriseTimeLabel = today.sunrise.timeLabel(using24HFormat),
        sunsetTimeLabel = today.sunset.timeLabel(using24HFormat)
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color(0xFF76B3CC),
        periodLabel = "Civil twilight",
        sunriseEndingEdgeLabel =
          "Civil dawn ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 6º below",
        sunsetEndingEdgeLabel =
          "Civil dusk ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 6º below",
        sunriseTimeLabel = today.civilTwilightBegin.timeLabel(using24HFormat),
        sunsetTimeLabel = today.civilTwilightEnd.timeLabel(using24HFormat)
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color(0xFF3D6475),
        periodLabel = "Nautical twilight",
        sunriseEndingEdgeLabel =
          "Nautical dawn ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 12º below",
        sunsetEndingEdgeLabel =
          "Nautical dusk ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 12º below",
        sunriseTimeLabel = today.nauticalTwilightBegin.timeLabel(using24HFormat),
        sunsetTimeLabel = today.nauticalTwilightEnd.timeLabel(using24HFormat)
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color(0xFF223F4D),
        periodLabel = "Astronomical twilight",
        sunriseEndingEdgeLabel =
          "Astronomical dawn ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 18º below",
        sunsetEndingEdgeLabel =
          "Astronomical dusk ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 18º below",
        sunriseTimeLabel = today.astronomicalTwilightBegin.timeLabel(using24HFormat),
        sunsetTimeLabel = today.astronomicalTwilightEnd.timeLabel(using24HFormat)
      ),
      DayChartSegment(sweepAngleDegrees = 72f, color = Color(0xFF172A33), periodLabel = "Night"),
    )
  }
}

private fun DrawScope.buildSunArcArrowHeadPath(
  dayMode: DayMode,
  arrowHeadCenterX: Float,
  arrowHeadDimension: Float
): Path =
  Path().apply {
    moveTo(arrowHeadCenterX - arrowHeadDimension / 2f, size.height / 2f)
    lineTo(
      x = arrowHeadCenterX,
      y =
        if (dayMode == DayMode.SUNRISE) size.height / 2f - arrowHeadDimension
        else size.height / 2f + arrowHeadDimension
    )
    lineTo(arrowHeadCenterX + arrowHeadDimension / 2f, size.height / 2f)
    close()
  }
