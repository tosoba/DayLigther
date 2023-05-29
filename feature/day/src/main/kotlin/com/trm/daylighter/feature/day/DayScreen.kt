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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.trm.daylighter.feature.day.ext.currentPeriodIn
import com.trm.daylighter.feature.day.ext.dayPeriodEndTime
import com.trm.daylighter.feature.day.ext.dayPeriodStartTime
import com.trm.daylighter.feature.day.ext.isPolarDayAtLocation
import com.trm.daylighter.feature.day.ext.isPolarNightAtLocation
import com.trm.daylighter.feature.day.ext.textColor
import com.trm.daylighter.feature.day.ext.textShadowColor
import com.trm.daylighter.feature.day.model.DayMode
import com.trm.daylighter.feature.day.model.DayPeriod
import java.lang.Float.max
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.asin
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
    viewModel.currentTimeAtCurrentLocation.collectAsStateWithLifecycle(
      initialValue = LocalTime.now()
    )
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
  now: LocalTime,
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
      if (changeValue is WithData) initialDayMode(changeValue.data.location.zoneId)
      else DayMode.SUNRISE
    )
  }
  LaunchedEffect(change) {
    if (changeValue is WithData) dayMode = initialDayMode(changeValue.data.location.zoneId)
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
  now: LocalTime,
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

    var appBarHeightPx by remember { mutableStateOf(0f) }

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
                appBarHeightPx = appBarHeightPx,
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
              appBarHeightPx = appBarHeightPx,
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
            .onGloballyPositioned { coordinates ->
              appBarHeightPx = coordinates.size.height.toFloat()
            }
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
            .onGloballyPositioned { coordinates ->
              appBarHeightPx = coordinates.size.height.toFloat()
            }
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
  val dayPeriod =
    remember(change) {
      change.value
        .map { (location, today) -> today.currentPeriodIn(location) }
        .dataOrElse(DayPeriod.DAY)
    }

  Surface(
    shape = CardDefaults.shape,
    color = dayPeriod.color(),
    shadowElevation = 6.dp,
    modifier = modifier
  ) {
    change.value.takeIfInstance<WithData<LocationSunriseSunsetChange>>()?.let {
      val (location, today, yesterday) = it.data
      if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(8.dp)
        ) {
          Clock(zoneId = location.zoneId, dayPeriod = dayPeriod)
          NowTimezoneDiffText(zoneId = location.zoneId, dayPeriod = dayPeriod)
          Spacer(modifier = Modifier.height(5.dp))
          DayLengthInfo(today = today, yesterday = yesterday, dayPeriod = dayPeriod)
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Clock(zoneId = location.zoneId, dayPeriod = dayPeriod)
            NowTimezoneDiffText(zoneId = location.zoneId, dayPeriod = dayPeriod)
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

  fun TextClock.onZoneIdOrDayPeriodUpdate() {
    timeZone = zoneId.id
    setTextColor(dayPeriod.textColor().toArgb())
    setShadowLayer(1f, 1f, 1f, dayPeriod.textShadowColor().toArgb())
  }

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
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        onZoneIdOrDayPeriodUpdate()
      }
    },
    update = { it.onZoneIdOrDayPeriodUpdate() },
    modifier = modifier
  )
}

@Composable
private fun NowTimezoneDiffText(zoneId: ZoneId, dayPeriod: DayPeriod) {
  val context = LocalContext.current
  Text(
    text = context.timeZoneDiffLabelBetween(ZonedDateTime.now(), ZonedDateTime.now(zoneId)),
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
    dayChartSegments(
      location = location,
      today = today,
      yesterday = yesterday,
      orientation = orientation,
      using24HFormat = DateFormat.is24HourFormat(LocalContext.current)
    )

  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall
  val textColor = MaterialTheme.colorScheme.onBackground

  val horizonLabel = stringResource(R.string.horizon)
  val dayLabel = stringResource(R.string.day)

  val nowLineColor = colorResource(id = commonR.color.now_line)

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

    if (today?.sunrise != null && today.sunset != null) {
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
    }

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
        val strokeWidth = 2f

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
              if (chartSegments[segmentIndex].periodLabel.startsWith(dayLabel)) 0f
              else endingEdgeLabelLayoutResult.size.height / 2f
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

    val angleDeltaDegrees = 6f
    chartSegments.forEach { segment ->
      rotate(degrees = (segment.periodLabelAngle - angleDeltaDegrees / 2f), pivot = chartCenter) {
        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(segment.periodLabel))
        drawText(
          textMeasurer = textMeasurer,
          text = segment.periodLabel,
          topLeft =
            Offset(
              x = chartCenter.x + chartRadius - textLayoutResult.size.width - textPadding,
              y =
                if (segment.periodLabel == dayLabel) {
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

private fun initialDayMode(zoneId: ZoneId): DayMode =
  if (LocalTime.now(zoneId).isBefore(LocalTime.NOON)) DayMode.SUNRISE else DayMode.SUNSET

@Composable
private fun dayChartSegments(
  location: Location?,
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
  val civilDawnLabel = stringResource(id = R.string.civil_dawn, edgeLabelSeparator)
  val civilDuskLabel = stringResource(id = R.string.civil_dusk, edgeLabelSeparator)
  val nauticalDawnLabel = stringResource(id = R.string.nautical_dawn, edgeLabelSeparator)
  val nauticalDuskLabel = stringResource(id = R.string.nautical_dusk, edgeLabelSeparator)
  val astronomicalDawnLabel = stringResource(id = R.string.astronomical_dawn, edgeLabelSeparator)
  val astronomicalDuskLabel = stringResource(id = R.string.astronomical_dusk, edgeLabelSeparator)

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
