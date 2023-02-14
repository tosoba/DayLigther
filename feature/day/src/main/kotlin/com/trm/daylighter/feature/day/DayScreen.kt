package com.trm.daylighter.feature.day

import android.content.res.Configuration
import android.graphics.Typeface
import android.text.format.DateFormat
import android.widget.TextClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
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
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.*
import com.trm.daylighter.core.common.util.takeIfInstance
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.composable.ZoomInButton
import com.trm.daylighter.core.ui.composable.ZoomOutButton
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.feature.day.model.DayMode
import java.lang.Float.max
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

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
  val sunrisePeriodStart: ZonedDateTime,
  val sunrisePeriodEnd: ZonedDateTime,
  val sunsetPeriodStart: ZonedDateTime,
  val sunsetPeriodEnd: ZonedDateTime,
  val sunriseEndingEdgeLabel: String = "",
  val sunsetEndingEdgeLabel: String = "",
  val sunriseTimeLabel: (() -> String)? = null,
  val sunsetTimeLabel: (() -> String)? = null,
  val sunriseDiffLabel: (() -> String)? = null,
  val sunsetDiffLabel: (() -> String)? = null,
)

private fun initialDayMode(today: SunriseSunset): DayMode {
  val now = ZonedDateTime.now(today.sunrise.zone)
  if (now.isBefore(today.sunrise)) return DayMode.SUNRISE
  else if (now.isAfter(today.sunset)) return DayMode.SUNSET

  val nowSeconds = now.toLocalTime().toSecondOfDay()
  val diffSunrise = abs(nowSeconds - today.sunrise.toLocalTime().toSecondOfDay())
  val diffSunset = abs(nowSeconds - today.sunset.toLocalTime().toSecondOfDay())
  return if (diffSunset < diffSunrise) DayMode.SUNSET else DayMode.SUNRISE
}

@Composable
private fun DayScreen(
  locationSunriseSunsetChange: Loadable<StableValue<LocationSunriseSunsetChange>>,
  locationsCount: Int,
  currentLocationIndex: Int,
  onDrawerMenuClick: () -> Unit,
  onChangeLocationIndex: (Int) -> Unit,
  onAddLocationClick: () -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var mapZoom by rememberSaveable { mutableStateOf(MapDefaults.INITIAL_LOCATION_ZOOM) }

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
        var dayMode by rememberSaveable {
          mutableStateOf(initialDayMode(locationSunriseSunsetChange.data.value.today))
        }
        SunriseSunset(
          locationSunriseSunsetChange = locationSunriseSunsetChange.data,
          dayMode = dayMode,
          locationsCount = locationsCount,
          currentLocationIndex = currentLocationIndex,
          onChangeLocationIndex = onChangeLocationIndex,
          mapZoom = mapZoom,
          onZoomInClick = { if (mapZoom < MapDefaults.MAX_ZOOM) ++mapZoom },
          onZoomOutClick = { if (mapZoom > MapDefaults.MIN_ZOOM) --mapZoom },
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
  locationSunriseSunsetChange: StableValue<LocationSunriseSunsetChange>,
  dayMode: DayMode,
  locationsCount: Int,
  currentLocationIndex: Int,
  onChangeLocationIndex: (Int) -> Unit,
  mapZoom: Double,
  onZoomInClick: () -> Unit,
  onZoomOutClick: () -> Unit,
  onDayModeNavClick: (DayMode) -> Unit,
  onDrawerMenuClick: () -> Unit,
) {
  val (drawerMenuButton, pagerBox, navigation, dayTimeCard, map, mapZoomControls) = createRefs()
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

  MapCard(
    locationSunriseSunsetChange = locationSunriseSunsetChange,
    mapZoom = mapZoom,
    modifier =
      Modifier.run {
          if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            width((LocalConfiguration.current.screenWidthDp * .4f).dp)
          } else {
            height((LocalConfiguration.current.screenHeightDp * .35f).dp)
          }
        }
        .aspectRatio(1f)
        .constrainAs(map) {
          top.linkTo(parent.top, 16.dp)
          if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            end.linkTo(parent.end, 16.dp)
          } else {
            end.linkTo(mapZoomControls.start, 5.dp)
          }
        }
  )

  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier =
        Modifier.constrainAs(mapZoomControls) {
          start.linkTo(map.start)
          end.linkTo(map.end)
          top.linkTo(map.bottom, 5.dp)
        }
    ) {
      ZoomInButton(mapZoom = mapZoom, onClick = onZoomInClick)
      Spacer(modifier = Modifier.width(5.dp))
      ZoomOutButton(mapZoom = mapZoom, onClick = onZoomOutClick)
    }
  } else {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier =
        Modifier.constrainAs(mapZoomControls) {
          end.linkTo(parent.end, 16.dp)
          top.linkTo(map.top)
          bottom.linkTo(map.bottom)
        }
    ) {
      ZoomInButton(mapZoom = mapZoom, onClick = onZoomInClick)
      Spacer(modifier = Modifier.height(5.dp))
      ZoomOutButton(mapZoom = mapZoom, onClick = onZoomOutClick)
    }
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

    ClockAndDayLengthCard(
      locationSunriseSunsetChange = locationSunriseSunsetChange,
      modifier =
        Modifier.constrainAs(dayTimeCard) {
          top.linkTo(drawerMenuButton.bottom, 10.dp)
          start.linkTo(parent.start, 16.dp)
        },
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

    ClockAndDayLengthCard(
      locationSunriseSunsetChange = locationSunriseSunsetChange,
      modifier =
        Modifier.constrainAs(dayTimeCard) {
          top.linkTo(parent.top, 16.dp)
          start.linkTo(navigation.end, 16.dp)
        },
    )
  }
}

@Composable
private fun MapCard(
  locationSunriseSunsetChange: StableValue<LocationSunriseSunsetChange>,
  mapZoom: Double,
  modifier: Modifier = Modifier,
) {
  val (location) = locationSunriseSunsetChange.value
  Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    modifier = modifier,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val mapView = rememberMapViewWithLifecycle()
      AndroidView(
        factory = { mapView },
        update = {
          it.setDefaultDisabledConfig()
          it.setPosition(
            latitude = location.latitude,
            longitude = location.longitude,
            zoom = mapZoom
          )
        }
      )
      Icon(
        painter = painterResource(id = commonR.drawable.marker),
        contentDescription = stringResource(id = commonR.string.location_marker),
        modifier = Modifier.align(Alignment.Center).size(36.dp)
      )
    }
  }
}

@Composable
private fun ClockAndDayLengthCard(
  locationSunriseSunsetChange: StableValue<LocationSunriseSunsetChange>,
  modifier: Modifier = Modifier,
) {
  val (_, today, yesterday) = locationSunriseSunsetChange.value
  Surface(
    shape = CardDefaults.shape,
    color = FloatingActionButtonDefaults.containerColor,
    contentColor = contentColorFor(FloatingActionButtonDefaults.containerColor),
    shadowElevation = 6.dp,
    modifier = modifier
  ) {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(5.dp))
        Clock(zoneId = today.sunrise.zone, modifier = Modifier.padding(horizontal = 8.dp))
        Spacer(modifier = Modifier.height(5.dp))
        DayLength(
          today = today,
          yesterday = yesterday,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
      }
    } else {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(5.dp))
        Clock(zoneId = today.sunrise.zone, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(modifier = Modifier.width(5.dp))
        DayLength(
          today = today,
          yesterday = yesterday,
          modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
      }
    }
  }
}

@Composable
private fun Clock(zoneId: ZoneId, modifier: Modifier = Modifier) {
  val labelMediumStyle = MaterialTheme.typography.labelMedium
  val resolver = LocalFontFamilyResolver.current
  val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
  val orientation = LocalConfiguration.current.orientation

  AndroidView(
    factory = { context ->
      TextClock(context).apply {
        format24Hour = "HH:mm:ss \n zz"
        format12Hour = "hh:mm:ss a \n zz"
        resolver
          .resolve(
            fontFamily = labelMediumStyle.fontFamily,
            fontWeight = labelMediumStyle.fontWeight ?: FontWeight.Normal,
            fontStyle = labelMediumStyle.fontStyle ?: FontStyle.Normal,
            fontSynthesis = labelMediumStyle.fontSynthesis ?: FontSynthesis.All,
          )
          .value
          .takeIfInstance<Typeface>()
          ?.let(this::setTypeface)
        textSize = if (orientation == Configuration.ORIENTATION_PORTRAIT) 18f else 16f
        setTextColor(textColor)
      }
    },
    update = { clockView -> clockView.timeZone = zoneId.id },
    modifier = modifier
  )
}

@Composable
private fun DayLength(
  today: SunriseSunset,
  yesterday: SunriseSunset,
  modifier: Modifier = Modifier
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    Box(modifier = Modifier.size(50.dp)) {
      val sunPainter =
        rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.sun))
      Image(
        painter = sunPainter,
        contentDescription = "",
        Modifier.align(Alignment.Center).size(40.dp)
      )
      Icon(
        painter = painterResource(id = R.drawable.clock),
        contentDescription = "",
        Modifier.align(Alignment.BottomEnd)
      )
    }

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
      val todayLength = LocalTime.ofSecondOfDay(today.dayLengthSeconds.toLong())
      val diffLength =
        LocalTime.ofSecondOfDay(abs(today.dayLengthSeconds - yesterday.dayLengthSeconds).toLong())
      val diffPrefix =
        when {
          today.dayLengthSeconds > yesterday.dayLengthSeconds -> "+"
          today.dayLengthSeconds < yesterday.dayLengthSeconds -> "-"
          else -> ""
        }

      Text(text = todayLength.format(DateTimeFormatter.ISO_LOCAL_TIME))
      Text(text = formatTimeDifference(diffPrefix, diffLength))
    }
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
        Icon(
          painter = painterResource(R.drawable.sunrise),
          contentDescription = stringResource(R.string.sunrise)
        )
      },
      label = { Text(text = stringResource(R.string.sunrise)) }
    )
    NavigationBarItem(
      selected = dayMode == DayMode.SUNSET,
      onClick = { onDayModeChange(DayMode.SUNSET) },
      icon = {
        Icon(
          painter = painterResource(R.drawable.sunset),
          contentDescription = stringResource(R.string.sunset)
        )
      },
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
        Icon(
          painter = painterResource(R.drawable.sunrise),
          contentDescription = stringResource(R.string.sunrise)
        )
      },
      label = { Text(text = stringResource(R.string.sunrise)) }
    )
    NavigationRailItem(
      selected = dayMode == DayMode.SUNSET,
      onClick = { onDayModeChange(DayMode.SUNSET) },
      icon = {
        Icon(
          painter = painterResource(R.drawable.sunset),
          contentDescription = stringResource(R.string.sunset)
        )
      },
      label = { Text(text = stringResource(R.string.sunset)) }
    )
    Spacer(modifier = Modifier.weight(1f))
  }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun SunriseSunsetChart(
  locationSunriseSunsetChange: StableValue<LocationSunriseSunsetChange>,
  dayMode: DayMode,
  modifier: Modifier = Modifier
) {
  val orientation = LocalConfiguration.current.orientation
  val (_, today, yesterday) = locationSunriseSunsetChange.value

  val chartSegments =
    dayChartSegments(
      today = today,
      yesterday = yesterday,
      orientation = orientation,
      using24HFormat = DateFormat.is24HourFormat(LocalContext.current)
    )

  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall

  val sunPainter = rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.sun))

  val chartSegmentGlowPaint = remember {
    Paint().apply {
      style = PaintingStyle.Stroke
      strokeWidth = 30f
    }
  }
  remember {
    chartSegmentGlowPaint.asFrameworkPaint().apply {
      val glowColor = Color.Yellow
      color = glowColor.copy(alpha = 0f).toArgb()
      setShadowLayer(30f, 0f, 0f, glowColor.copy(alpha = .5f).toArgb())
    }
  }

  var now by remember { mutableStateOf(ZonedDateTime.now(today.sunrise.zone)) }
  val remainingTimestamps = remember {
    LinkedList(
      today.run {
        listOf(
            astronomicalTwilightBegin,
            astronomicalTwilightEnd,
            civilTwilightBegin,
            civilTwilightEnd,
            nauticalTwilightBegin,
            nauticalTwilightEnd,
            sunrise,
            sunset
          )
          .filter { it.isAfter(now) }
          .sorted()
      }
    )
  }
  LaunchedEffect(Unit) {
    flow {
        while (currentCoroutineContext().isActive && remainingTimestamps.isNotEmpty()) {
          emit(ZonedDateTime.now(today.sunrise.zone))
          delay(1000L)
        }
      }
      .filter { remainingTimestamps.isNotEmpty() && remainingTimestamps.first().isBefore(it) }
      .onEach {
        remainingTimestamps.removeFirst()
        now = ZonedDateTime.now(today.sunrise.zone)
      }
      .launchIn(this)
  }

  Canvas(modifier = modifier) {
    val topLeftOffset =
      Offset(
        -size.height * if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.7f else 1f,
        -size.height * .5f
      )
    val segmentSize = Size(size.height, size.height) * 2f
    var startAngle = -180f

    fun DrawScope.drawChartSegment(segment: DayChartSegment) {
      val isCurrent =
        (dayMode == DayMode.SUNRISE &&
          now.isAfter(segment.sunrisePeriodStart) &&
          now.isBefore(segment.sunrisePeriodEnd)) ||
          (dayMode == DayMode.SUNSET &&
            now.isAfter(segment.sunsetPeriodStart) &&
            now.isBefore(segment.sunsetPeriodEnd))
      clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
        if (isCurrent) {
          drawIntoCanvas {
            it.drawArc(
              left = topLeftOffset.x,
              top = topLeftOffset.y,
              bottom = topLeftOffset.y + segmentSize.height,
              right = topLeftOffset.x + segmentSize.width,
              startAngle = startAngle,
              sweepAngle = segment.sweepAngleDegrees,
              useCenter = false,
              paint = chartSegmentGlowPaint,
            )
          }
        }
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
        text = timeAndDiffLabel,
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
    val sunArcSweepAngle = if (orientation == Configuration.ORIENTATION_PORTRAIT) 10f else 15f
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
  yesterday: SunriseSunset,
  orientation: Int,
  using24HFormat: Boolean
): List<DayChartSegment> {
  val sunrise = stringResource(id = R.string.sunrise)
  val sunset = stringResource(id = R.string.sunset)
  return remember(today, yesterday) {
    listOf(
      DayChartSegment(
        sweepAngleDegrees = 180f,
        color = Color(0xFFB9D9E5),
        periodLabel = "Day",
        sunrisePeriodStart = today.sunrise,
        sunrisePeriodEnd = today.sunset,
        sunsetPeriodStart = today.sunrise,
        sunsetPeriodEnd = today.sunset,
        sunriseEndingEdgeLabel = sunrise,
        sunsetEndingEdgeLabel = sunset,
        sunriseTimeLabel = today.sunrise.timeLabel(using24HFormat),
        sunsetTimeLabel = today.sunset.timeLabel(using24HFormat),
        sunriseDiffLabel = {
          timeDifferenceLabel(yesterday.sunrise.toLocalTime(), today.sunrise.toLocalTime())
        },
        sunsetDiffLabel = {
          timeDifferenceLabel(yesterday.sunset.toLocalTime(), today.sunset.toLocalTime())
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color(0xFF76B3CC),
        periodLabel = "Civil twilight",
        sunrisePeriodStart = today.civilTwilightBegin,
        sunrisePeriodEnd = today.sunrise,
        sunsetPeriodStart = today.sunset,
        sunsetPeriodEnd = today.civilTwilightEnd,
        sunriseEndingEdgeLabel =
          "Civil dawn ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 6º below",
        sunsetEndingEdgeLabel =
          "Civil dusk ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 6º below",
        sunriseTimeLabel = today.civilTwilightBegin.timeLabel(using24HFormat),
        sunsetTimeLabel = today.civilTwilightEnd.timeLabel(using24HFormat),
        sunriseDiffLabel = {
          timeDifferenceLabel(
            yesterday.civilTwilightBegin.toLocalTime(),
            today.civilTwilightBegin.toLocalTime()
          )
        },
        sunsetDiffLabel = {
          timeDifferenceLabel(
            yesterday.civilTwilightEnd.toLocalTime(),
            today.civilTwilightEnd.toLocalTime()
          )
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color(0xFF3D6475),
        periodLabel = "Nautical twilight",
        sunrisePeriodStart = today.nauticalTwilightBegin,
        sunrisePeriodEnd = today.civilTwilightBegin,
        sunsetPeriodStart = today.civilTwilightEnd,
        sunsetPeriodEnd = today.nauticalTwilightEnd,
        sunriseEndingEdgeLabel =
          "Nautical dawn ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 12º below",
        sunsetEndingEdgeLabel =
          "Nautical dusk ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 12º below",
        sunriseTimeLabel = today.nauticalTwilightBegin.timeLabel(using24HFormat),
        sunsetTimeLabel = today.nauticalTwilightEnd.timeLabel(using24HFormat),
        sunriseDiffLabel = {
          timeDifferenceLabel(
            yesterday.nauticalTwilightBegin.toLocalTime(),
            today.nauticalTwilightBegin.toLocalTime()
          )
        },
        sunsetDiffLabel = {
          timeDifferenceLabel(
            yesterday.nauticalTwilightEnd.toLocalTime(),
            today.nauticalTwilightEnd.toLocalTime()
          )
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color(0xFF223F4D),
        periodLabel = "Astronomical twilight",
        sunrisePeriodStart = today.astronomicalTwilightBegin,
        sunrisePeriodEnd = today.nauticalTwilightBegin,
        sunsetPeriodStart = today.nauticalTwilightEnd,
        sunsetPeriodEnd = today.astronomicalTwilightEnd,
        sunriseEndingEdgeLabel =
          "Astronomical dawn ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 18º below",
        sunsetEndingEdgeLabel =
          "Astronomical dusk ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "} 18º below",
        sunriseTimeLabel = today.astronomicalTwilightBegin.timeLabel(using24HFormat),
        sunsetTimeLabel = today.astronomicalTwilightEnd.timeLabel(using24HFormat),
        sunriseDiffLabel = {
          timeDifferenceLabel(
            yesterday.astronomicalTwilightBegin.toLocalTime(),
            today.astronomicalTwilightBegin.toLocalTime()
          )
        },
        sunsetDiffLabel = {
          timeDifferenceLabel(
            yesterday.astronomicalTwilightEnd.toLocalTime(),
            today.astronomicalTwilightEnd.toLocalTime()
          )
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 72f,
        color = Color(0xFF172A33),
        periodLabel = "Night",
        sunrisePeriodStart =
          ZonedDateTime.ofLocal(today.date.atStartOfDay(), today.sunrise.zone, null),
        sunrisePeriodEnd = today.astronomicalTwilightBegin,
        sunsetPeriodStart = today.astronomicalTwilightEnd,
        sunsetPeriodEnd =
          ZonedDateTime.ofLocal(today.date.plusDays(1L).atStartOfDay(), today.sunrise.zone, null),
      ),
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