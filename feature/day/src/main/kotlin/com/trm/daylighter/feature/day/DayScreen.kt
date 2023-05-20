package com.trm.daylighter.feature.day

import android.content.res.Configuration
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.TextClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.colorResource
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.*
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.composable.*
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.theme.*
import com.trm.daylighter.feature.day.ext.color
import com.trm.daylighter.feature.day.ext.currentPeriod
import com.trm.daylighter.feature.day.ext.textColor
import com.trm.daylighter.feature.day.ext.textShadowColor
import com.trm.daylighter.feature.day.model.DayMode
import com.trm.daylighter.feature.day.model.DayPeriod
import java.lang.Float.max
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

const val dayRoute = "day_route"

@Composable
fun DayRoute(
  onDrawerMenuClick: () -> Unit,
  onAddLocation: () -> Unit,
  onEditLocation: (Long) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val change: State<StableLoadable<LocationSunriseSunsetChange>> =
    viewModel.currentLocationSunriseSunsetChangeFlow.collectAsStateWithLifecycle(
      initialValue = StableValue(LoadingFirst)
    )
  val now =
    viewModel.nowAtCurrentLocation.collectAsStateWithLifecycle(initialValue = ZonedDateTime.now())
  val locationsCount = viewModel.locationCountFlow.collectAsStateWithLifecycle(initialValue = 0)

  DayScreen(
    change = change.value,
    now = now.value,
    locationsCount = locationsCount.value,
    currentLocationIndex = viewModel.currentLocationIndex,
    onDrawerMenuClick = onDrawerMenuClick,
    onChangeLocationIndex = viewModel::changeLocation,
    onAddLocationClick = onAddLocation,
    onEditLocationClick = onEditLocation,
    onRetryClick = viewModel::reloadLocation,
    modifier = modifier
  )
}

@Composable
private fun DayScreen(
  change: StableLoadable<LocationSunriseSunsetChange>,
  now: ZonedDateTime,
  locationsCount: Int,
  currentLocationIndex: Int,
  onDrawerMenuClick: () -> Unit,
  onChangeLocationIndex: (Int) -> Unit,
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val changeValue = change.value
  var dayMode by rememberSaveable {
    mutableStateOf(
      if (changeValue is WithData) initialDayMode(changeValue.data.today.sunrise.zone)
      else DayMode.SUNRISE
    )
  }
  LaunchedEffect(change) {
    if (changeValue is WithData) dayMode = initialDayMode(changeValue.data.today.sunrise.zone)
  }

  SunriseSunset(
    change = change,
    dayMode = dayMode,
    now = now,
    locationsCount = locationsCount,
    currentLocationIndex = currentLocationIndex,
    onChangeLocationIndex = onChangeLocationIndex,
    onDayModeNavClick = { dayMode = it },
    onDrawerMenuClick = onDrawerMenuClick,
    onAddLocationClick = onAddLocationClick,
    onEditLocationClick = onEditLocationClick,
    onRetryClick = onRetryClick,
    modifier = modifier,
  )
}

@Composable
private fun DrawerMenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  SmallFloatingActionButton(onClick = onClick, modifier = modifier) {
    Icon(
      imageVector = Icons.Filled.Menu,
      contentDescription = stringResource(R.string.application_menu)
    )
  }
}

@Composable
private fun EditLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  @Composable
  fun EditLocationIcon() {
    Icon(
      imageVector = Icons.Filled.Edit,
      contentDescription = stringResource(R.string.edit_location)
    )
  }

  val orientation = LocalConfiguration.current.orientation
  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
    FloatingActionButton(onClick = onClick, modifier = modifier) { EditLocationIcon() }
  } else {
    SmallFloatingActionButton(onClick = onClick, modifier = modifier) { EditLocationIcon() }
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun SunriseSunset(
  change: StableLoadable<LocationSunriseSunsetChange>,
  dayMode: DayMode,
  now: ZonedDateTime,
  locationsCount: Int,
  currentLocationIndex: Int,
  onChangeLocationIndex: (Int) -> Unit,
  onDayModeNavClick: (DayMode) -> Unit,
  onDrawerMenuClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ConstraintLayout(modifier = modifier) {
    val (topAppBar, mainContent, navigation, dayTimeCard, editLocationButton) = createRefs()

    val orientation = LocalConfiguration.current.orientation
    val changeValue = change.value

    val pagerState = rememberPagerState(initialPage = currentLocationIndex)
    LaunchedEffect(pagerState) {
      snapshotFlow(pagerState::currentPage).collect(onChangeLocationIndex)
    }

    Box(
      modifier =
        Modifier.constrainAs(mainContent) {
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
      Crossfade(targetState = changeValue !is Empty, modifier = Modifier.fillMaxSize()) {
        pagerVisible ->
        if (pagerVisible) {
          HorizontalPager(
            count = locationsCount,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              SunriseSunsetChart(
                change = change,
                dayMode = dayMode,
                now = now,
                modifier = Modifier.fillMaxSize()
              )

              if (changeValue is Failed) {
                InfoButtonCard(
                  infoText = stringResource(R.string.error_occurred),
                  actionText = stringResource(commonR.string.retry),
                  onButtonClick = onRetryClick,
                  modifier = Modifier.align(Alignment.Center)
                )
              } else if (changeValue is Loading) {
                LinearProgressIndicator(
                  modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                )
              }
            }
          }

          HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            activeColor = MaterialTheme.colorScheme.onBackground,
          )
        } else {
          Box(modifier = Modifier.fillMaxSize()) {
            SunriseSunsetChart(
              change = change,
              dayMode = dayMode,
              now = now,
              modifier = Modifier.fillMaxSize()
            )

            InfoButtonCard(
              infoText = stringResource(R.string.no_saved_locations_add_one),
              actionText = stringResource(commonR.string.add_location),
              onButtonClick = onAddLocationClick,
              modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
          }
        }
      }
    }

    val topBarGradient =
      Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.surface,
        .25f to MaterialTheme.colorScheme.surface,
        1f to Color.Transparent
      )

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      DayTopAppBar(
        change = change,
        modifier =
          Modifier.constrainAs(topAppBar) {
              linkTo(mainContent.start, mainContent.end)
              top.linkTo(parent.top)
            }
            .background(topBarGradient)
            .padding(10.dp),
        navigationIcon = { DrawerMenuButton(onClick = onDrawerMenuClick) }
      )

      AnimatedVisibility(
        visible = changeValue is Ready,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier =
          Modifier.constrainAs(dayTimeCard) {
            top.linkTo(topAppBar.bottom, 10.dp)
            end.linkTo(parent.end, 16.dp)
          },
      ) {
        ClockAndDayLengthCard(change = change)
      }

      NavigationBar(
        content = {
          SunriseSunsetNavigationBarContent(
            dayMode = dayMode,
            itemsEnabled = changeValue is Ready,
            onDayModeChange = onDayModeNavClick
          )
        },
        modifier =
          Modifier.constrainAs(navigation) {
            linkTo(mainContent.bottom, parent.bottom)
            linkTo(parent.start, parent.end)
          }
      )

      AnimatedVisibility(
        visible = changeValue is Ready,
        modifier =
          Modifier.constrainAs(editLocationButton) {
            bottom.linkTo(navigation.top, 16.dp)
            end.linkTo(parent.end, 16.dp)
          }
      ) {
        EditLocationButton(
          onClick = { if (changeValue is Ready) onEditLocationClick(changeValue.data.location.id) },
        )
      }
    } else {
      DayTopAppBar(
        change = change,
        modifier =
          Modifier.constrainAs(topAppBar) {
              linkTo(navigation.end, mainContent.end)
              top.linkTo(parent.top)
              height = Dimension.wrapContent
              width = Dimension.fillToConstraints
            }
            .background(topBarGradient)
            .padding(10.dp)
      )

      NavigationRail(
        header = {
          DrawerMenuButton(onClick = onDrawerMenuClick, modifier = Modifier.padding(top = 8.dp))
        },
        content = {
          SunriseSunsetNavigationRailContent(
            dayMode = dayMode,
            itemsEnabled = changeValue is Ready,
            onDayModeChange = onDayModeNavClick,
            footer = {
              AnimatedVisibility(
                visible = changeValue is Ready,
                modifier = Modifier.padding(bottom = 8.dp)
              ) {
                EditLocationButton(
                  onClick = {
                    if (changeValue is Ready) onEditLocationClick(changeValue.data.location.id)
                  }
                )
              }
            }
          )
        },
        modifier =
          Modifier.constrainAs(navigation) {
            start.linkTo(parent.start)
            linkTo(parent.top, parent.bottom)
          },
      )

      AnimatedVisibility(
        visible = changeValue is Ready,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier =
          Modifier.constrainAs(dayTimeCard) {
            top.linkTo(topAppBar.bottom)
            end.linkTo(parent.end, 16.dp)
          },
      ) {
        ClockAndDayLengthCard(change = change)
      }
    }
  }
}

@Composable
private fun InfoButtonCard(
  infoText: String,
  actionText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(shape = CardDefaults.shape, shadowElevation = 6.dp, modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(10.dp)
    ) {
      Text(text = infoText, fontSize = 20.sp, textAlign = TextAlign.Center)
      Spacer(modifier = Modifier.height(10.dp))
      Button(onClick = onButtonClick) { Text(text = actionText) }
    }
  }
}

@Composable
private fun DayTopAppBar(
  change: StableLoadable<LocationSunriseSunsetChange>,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {}
) {
  val changeValue = change.value
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    navigationIcon()

    Crossfade(targetState = changeValue is Ready, modifier = Modifier.weight(1f)) { changeReady ->
      if (changeReady) {
        Text(
          text = changeValue.map { (location) -> location.name }.dataOrElse(""),
          style = MaterialTheme.typography.titleMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      } else {
        Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}

@Composable
private fun ClockAndDayLengthCard(
  change: StableLoadable<LocationSunriseSunsetChange>,
  modifier: Modifier = Modifier,
) {
  val changeValue = change.value
  val dayPeriod = changeValue.map { it.today.currentPeriod() }.dataOrElse(DayPeriod.DAY)

  Surface(
    shape = CardDefaults.shape,
    color = dayPeriod.color(),
    shadowElevation = 6.dp,
    modifier = modifier
  ) {
    if (changeValue is WithData) {
      val (_, today, yesterday) = changeValue.data
      if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(8.dp)
        ) {
          Clock(zoneId = today.sunrise.zone, dayPeriod = dayPeriod)
          NowTimezoneDiffText(dateTime = today.sunrise, dayPeriod = dayPeriod)
          Spacer(modifier = Modifier.height(5.dp))
          DayLengthInfo(today = today, yesterday = yesterday, dayPeriod = dayPeriod)
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Clock(zoneId = today.sunrise.zone, dayPeriod = dayPeriod)
            NowTimezoneDiffText(dateTime = today.sunrise, dayPeriod = dayPeriod)
          }
          Spacer(modifier = Modifier.width(5.dp))
          DayLengthInfo(today = today, yesterday = yesterday, dayPeriod = dayPeriod)
        }
      }
    }
  }
}

@Composable
private fun Clock(zoneId: ZoneId, dayPeriod: DayPeriod, modifier: Modifier = Modifier) {
  val textStyle = MaterialTheme.typography.labelLarge
  val resolver = LocalFontFamilyResolver.current

  AndroidView(
    factory = { context ->
      TextClock(context).apply {
        format24Hour = context.getString(commonR.string.time_format_24_h)
        format12Hour = context.getString(commonR.string.time_format_12_h)
        resolver
          .resolve(
            fontFamily = textStyle.fontFamily,
            fontWeight = textStyle.fontWeight ?: FontWeight.Normal,
            fontStyle = textStyle.fontStyle ?: FontStyle.Normal,
            fontSynthesis = textStyle.fontSynthesis ?: FontSynthesis.All,
          )
          .value
          .takeIfInstance<Typeface>()
          ?.let(this::setTypeface)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setTextColor(dayPeriod.textColor().toArgb())
        setShadowLayer(1f, 1f, 1f, dayPeriod.textShadowColor().toArgb())
      }
    },
    update = { clockView -> clockView.timeZone = zoneId.id },
    modifier = modifier
  )
}

@Composable
private fun NowTimezoneDiffText(dateTime: ZonedDateTime, dayPeriod: DayPeriod) {
  val context = LocalContext.current
  Text(
    text = context.timeZoneDiffLabelBetween(ZonedDateTime.now(), dateTime),
    textAlign = TextAlign.Center,
    fontSize = 12.sp,
    overflow = TextOverflow.Ellipsis,
    color = dayPeriod.textColor(),
    style =
      MaterialTheme.typography.bodySmall.copy(
        shadow =
          Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
      )
  )
}

@Composable
private fun DayLengthInfo(today: SunriseSunset, yesterday: SunriseSunset, dayPeriod: DayPeriod) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    val todayLength = LocalTime.ofSecondOfDay(today.dayLengthSeconds.toLong())
    val dayLengthDiffTime = dayLengthDiffTime(today.dayLengthSeconds, yesterday.dayLengthSeconds)
    val diffPrefix =
      dayLengthDiffPrefix(
        todayLengthSeconds = today.dayLengthSeconds,
        yesterdayLengthSeconds = yesterday.dayLengthSeconds
      )
    Text(
      text = stringResource(id = R.string.day_length_label),
      color = dayPeriod.textColor(),
      style =
        MaterialTheme.typography.bodyLarge.copy(
          shadow =
            Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
        )
    )
    Row {
      Text(
        text = todayLength.format(DateTimeFormatter.ISO_LOCAL_TIME),
        color = dayPeriod.textColor(),
        style =
          MaterialTheme.typography.bodyMedium.copy(
            shadow =
              Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
          )
      )
      Text(text = " ")
      Text(
        text = formatTimeDifference(diffPrefix, dayLengthDiffTime),
        color =
          when (diffPrefix) {
            "+" -> Color.Green
            "-" -> Color.Red
            else -> light_onDayColor
          },
        style =
          MaterialTheme.typography.bodyMedium.copy(
            shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 1f)
          )
      )
    }
  }
}

@Composable
private fun RowScope.SunriseSunsetNavigationBarContent(
  itemsEnabled: Boolean,
  dayMode: DayMode,
  onDayModeChange: (DayMode) -> Unit
) {
  NavigationBarItem(
    selected = itemsEnabled && dayMode == DayMode.SUNRISE,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNRISE) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunrise),
        contentDescription = stringResource(R.string.sunrise),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(R.string.sunrise),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    }
  )
  NavigationBarItem(
    selected = itemsEnabled && dayMode == DayMode.SUNSET,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNSET) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunset),
        contentDescription = stringResource(R.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(R.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    }
  )
}

@Composable
private fun ColumnScope.SunriseSunsetNavigationRailContent(
  itemsEnabled: Boolean,
  dayMode: DayMode,
  onDayModeChange: (DayMode) -> Unit,
  footer: @Composable () -> Unit
) {
  Spacer(modifier = Modifier.weight(1f))

  NavigationRailItem(
    selected = itemsEnabled && dayMode == DayMode.SUNRISE,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNRISE) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunrise),
        contentDescription = stringResource(R.string.sunrise),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(R.string.sunrise),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    }
  )

  NavigationRailItem(
    selected = itemsEnabled && dayMode == DayMode.SUNSET,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNSET) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunset),
        contentDescription = stringResource(R.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(R.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    }
  )

  Spacer(modifier = Modifier.weight(1f))

  footer()
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun SunriseSunsetChart(
  change: StableLoadable<LocationSunriseSunsetChange>,
  dayMode: DayMode,
  now: ZonedDateTime,
  modifier: Modifier = Modifier
) {
  val changeValue = change.value

  val orientation = LocalConfiguration.current.orientation
  val today = if (changeValue is WithData) changeValue.data.today else null
  val yesterday = if (changeValue is WithData) changeValue.data.yesterday else null

  val chartSegments =
    dayChartSegments(
      today = today,
      yesterday = yesterday,
      orientation = orientation,
      using24HFormat = DateFormat.is24HourFormat(LocalContext.current)
    )

  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall
  val textColor = MaterialTheme.colorScheme.onBackground

  val sunPainter =
    rememberVectorPainter(image = ImageVector.vectorResource(id = commonR.drawable.sun))

  val chartSegmentGlowPaint = remember {
    Paint().apply {
      style = PaintingStyle.Stroke
      strokeWidth = 75f
    }
  }
  val glowColor = colorResource(id = commonR.color.sun_outline)
  remember {
    chartSegmentGlowPaint.asFrameworkPaint().apply {
      color = glowColor.copy(alpha = 0f).toArgb()
      setShadowLayer(60f, 0f, 0f, glowColor.copy(alpha = .5f).toArgb())
    }
  }

  val horizonLabel = stringResource(R.string.horizon)

  Canvas(modifier = modifier) {
    val topLeftOffset =
      Offset(
        -size.height * if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.7f else 1f,
        -size.height * .5f
      )
    val segmentSize = Size(size.height, size.height) * 2f
    var startAngle = -180f

    fun DrawScope.drawChartSegment(segment: DayChartSegment) {
      val isCurrent = segment.isCurrent(now, dayMode)
      clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
        drawIntoCanvas {
          it.drawArc(
            left = topLeftOffset.x,
            top = topLeftOffset.y,
            bottom = topLeftOffset.y + segmentSize.height,
            right = topLeftOffset.x + segmentSize.width,
            startAngle = startAngle,
            sweepAngle = segment.sweepAngleDegrees,
            useCenter = false,
            paint =
              if (isCurrent) {
                chartSegmentGlowPaint
              } else {
                val paint =
                  Paint().apply {
                    style = PaintingStyle.Stroke
                    strokeWidth = 50f
                  }
                paint.asFrameworkPaint().apply {
                  color = segment.color.copy(alpha = 0f).toArgb()
                  setShadowLayer(40f, 0f, 0f, segment.color.copy(alpha = .75f).toArgb())
                }
                paint
              },
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
      }
    }

    chartSegments.forEach { segment ->
      drawChartSegment(segment)
      startAngle += segment.sweepAngleDegrees
    }

    if (changeValue !is Ready) return@Canvas

    val chartRadius = segmentSize.maxDimension / 2f
    val chartCenter = Offset(topLeftOffset.x + chartRadius, size.height / 2f)
    var currentAngleDegrees = 0f
    val angleIncrementDegrees = 6f
    val textPadding = 3.dp.toPx()
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    val horizonLayoutResult = textMeasurer.measure(text = AnnotatedString(horizonLabel))
    drawText(
      textMeasurer = textMeasurer,
      text = horizonLabel,
      topLeft =
        Offset(
          x = size.width - horizonLayoutResult.size.width - textPadding,
          y = chartCenter.y - horizonLayoutResult.size.height - textPadding
        ),
      style = labelSmallTextStyle.copy(textAlign = TextAlign.Right, color = textColor),
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
        style = labelSmallTextStyle.copy(textAlign = TextAlign.Right, color = textColor),
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

private data class DayChartSegment(
  val sweepAngleDegrees: Float,
  val color: Color,
  val periodLabel: String,
  val sunrisePeriodStart: ZonedDateTime?,
  val sunrisePeriodEnd: ZonedDateTime?,
  val sunsetPeriodStart: ZonedDateTime?,
  val sunsetPeriodEnd: ZonedDateTime?,
  val sunriseEndingEdgeLabel: String = "",
  val sunsetEndingEdgeLabel: String = "",
  val sunriseTimeLabel: (() -> String)? = null,
  val sunsetTimeLabel: (() -> String)? = null,
  val sunriseDiffLabel: (() -> String)? = null,
  val sunsetDiffLabel: (() -> String)? = null,
) {
  fun isCurrent(now: ZonedDateTime, dayMode: DayMode): Boolean =
    hasAllTimestamps && (isInSunrisePeriod(now, dayMode) || isInSunsetPeriod(now, dayMode))

  private val hasAllTimestamps: Boolean
    get() =
      sunrisePeriodStart != null &&
        sunrisePeriodEnd != null &&
        sunsetPeriodStart != null &&
        sunsetPeriodEnd != null

  private fun isInSunrisePeriod(now: ZonedDateTime, dayMode: DayMode): Boolean =
    dayMode == DayMode.SUNRISE && now.isAfter(sunrisePeriodStart) && now.isBefore(sunrisePeriodEnd)

  private fun isInSunsetPeriod(now: ZonedDateTime, dayMode: DayMode): Boolean =
    dayMode == DayMode.SUNSET && now.isAfter(sunsetPeriodStart) && now.isBefore(sunsetPeriodEnd)
}

private fun initialDayMode(zoneId: ZoneId): DayMode =
  if (LocalTime.now(zoneId).isBefore(LocalTime.NOON)) DayMode.SUNRISE else DayMode.SUNSET

@Composable
private fun dayChartSegments(
  today: SunriseSunset?,
  yesterday: SunriseSunset?,
  orientation: Int,
  using24HFormat: Boolean
): List<DayChartSegment> {
  val sunriseLabel = stringResource(id = R.string.sunrise)
  val sunsetLabel = stringResource(id = R.string.sunset)
  val dayLabel = stringResource(R.string.day)
  val civilTwilightLabel = stringResource(R.string.civil_twilight)
  val nauticalTwilightLabel = stringResource(R.string.nautical_twilight)
  val astronomicalTwilightLabel = stringResource(R.string.astronomical_twilight)
  val nightLabel = stringResource(R.string.night)
  val edgeLabelSeparator = if (orientation == Configuration.ORIENTATION_PORTRAIT) "\n" else " - "
  val civilDawnLabel = stringResource(id = R.string.civil_dawn, edgeLabelSeparator)
  val civilDuskLabel = stringResource(id = R.string.civil_dusk, edgeLabelSeparator)
  val nauticalDawnLabel = stringResource(id = R.string.nautical_dawn, edgeLabelSeparator)
  val nauticalDuskLabel = stringResource(id = R.string.nautical_dusk, edgeLabelSeparator)
  val astronomicalDawnLabel = stringResource(id = R.string.astronomical_dawn, edgeLabelSeparator)
  val astronomicalDuskLabel = stringResource(id = R.string.astronomical_dusk, edgeLabelSeparator)
  return remember(today, yesterday, using24HFormat) {
    listOf(
      DayChartSegment(
        sweepAngleDegrees = 180f,
        color = dayColor,
        periodLabel = dayLabel,
        sunrisePeriodStart = today?.sunrise,
        sunrisePeriodEnd = today?.sunset,
        sunsetPeriodStart = today?.sunrise,
        sunsetPeriodEnd = today?.sunset,
        sunriseEndingEdgeLabel = sunriseLabel,
        sunsetEndingEdgeLabel = sunsetLabel,
        sunriseTimeLabel = today?.sunrise?.timeLabel(using24HFormat) ?: { "" },
        sunsetTimeLabel = today?.sunset?.timeLabel(using24HFormat) ?: { "" },
        sunriseDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(yesterday.sunrise.toLocalTime(), today.sunrise.toLocalTime())
          } else {
            ""
          }
        },
        sunsetDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(yesterday.sunset.toLocalTime(), today.sunset.toLocalTime())
          } else {
            ""
          }
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = civilTwilightColor,
        periodLabel = civilTwilightLabel,
        sunrisePeriodStart = today?.civilTwilightBegin,
        sunrisePeriodEnd = today?.sunrise,
        sunsetPeriodStart = today?.sunset,
        sunsetPeriodEnd = today?.civilTwilightEnd,
        sunriseEndingEdgeLabel = civilDawnLabel,
        sunsetEndingEdgeLabel = civilDuskLabel,
        sunriseTimeLabel = today?.civilTwilightBegin?.timeLabel(using24HFormat) ?: { "" },
        sunsetTimeLabel = today?.civilTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
        sunriseDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(
              yesterday.civilTwilightBegin.toLocalTime(),
              today.civilTwilightBegin.toLocalTime()
            )
          } else {
            ""
          }
        },
        sunsetDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(
              yesterday.civilTwilightEnd.toLocalTime(),
              today.civilTwilightEnd.toLocalTime()
            )
          } else {
            ""
          }
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = nauticalTwilightColor,
        periodLabel = nauticalTwilightLabel,
        sunrisePeriodStart = today?.nauticalTwilightBegin,
        sunrisePeriodEnd = today?.civilTwilightBegin,
        sunsetPeriodStart = today?.civilTwilightEnd,
        sunsetPeriodEnd = today?.nauticalTwilightEnd,
        sunriseEndingEdgeLabel = nauticalDawnLabel,
        sunsetEndingEdgeLabel = nauticalDuskLabel,
        sunriseTimeLabel = today?.nauticalTwilightBegin?.timeLabel(using24HFormat) ?: { "" },
        sunsetTimeLabel = today?.nauticalTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
        sunriseDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(
              yesterday.nauticalTwilightBegin.toLocalTime(),
              today.nauticalTwilightBegin.toLocalTime()
            )
          } else {
            ""
          }
        },
        sunsetDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(
              yesterday.nauticalTwilightEnd.toLocalTime(),
              today.nauticalTwilightEnd.toLocalTime()
            )
          } else {
            ""
          }
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = astronomicalTwilightColor,
        periodLabel = astronomicalTwilightLabel,
        sunrisePeriodStart = today?.astronomicalTwilightBegin,
        sunrisePeriodEnd = today?.nauticalTwilightBegin,
        sunsetPeriodStart = today?.nauticalTwilightEnd,
        sunsetPeriodEnd = today?.astronomicalTwilightEnd,
        sunriseEndingEdgeLabel = astronomicalDawnLabel,
        sunsetEndingEdgeLabel = astronomicalDuskLabel,
        sunriseTimeLabel = today?.astronomicalTwilightBegin?.timeLabel(using24HFormat) ?: { "" },
        sunsetTimeLabel = today?.astronomicalTwilightEnd?.timeLabel(using24HFormat) ?: { "" },
        sunriseDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(
              yesterday.astronomicalTwilightBegin.toLocalTime(),
              today.astronomicalTwilightBegin.toLocalTime()
            )
          } else {
            ""
          }
        },
        sunsetDiffLabel = {
          if (today != null && yesterday != null) {
            timeDifferenceLabel(
              yesterday.astronomicalTwilightEnd.toLocalTime(),
              today.astronomicalTwilightEnd.toLocalTime()
            )
          } else {
            ""
          }
        }
      ),
      DayChartSegment(
        sweepAngleDegrees = 72f,
        color = nightColor,
        periodLabel = nightLabel,
        sunrisePeriodStart =
          today?.let { ZonedDateTime.ofLocal(it.date.atStartOfDay(), it.sunrise.zone, null) },
        sunrisePeriodEnd = today?.astronomicalTwilightBegin,
        sunsetPeriodStart = today?.astronomicalTwilightEnd,
        sunsetPeriodEnd =
          today?.let {
            ZonedDateTime.ofLocal(it.date.plusDays(1L).atStartOfDay(), it.sunrise.zone, null)
          },
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
