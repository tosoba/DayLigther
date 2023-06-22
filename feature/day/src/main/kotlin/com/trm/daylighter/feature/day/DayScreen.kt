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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.*
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.util.ext.dayLengthSecondsAtLocation
import com.trm.daylighter.core.domain.util.ext.isPolarDayAtLocation
import com.trm.daylighter.core.domain.util.ext.isPolarNightAtLocation
import com.trm.daylighter.core.ui.composable.*
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.*
import com.trm.daylighter.feature.day.ext.color
import com.trm.daylighter.feature.day.ext.currentPeriodIn
import com.trm.daylighter.feature.day.ext.dayPeriodEndTime
import com.trm.daylighter.feature.day.ext.dayPeriodStartTime
import com.trm.daylighter.feature.day.ext.textColor
import com.trm.daylighter.feature.day.ext.textShadowColor
import com.trm.daylighter.feature.day.model.DayMode
import com.trm.daylighter.feature.day.model.DayPeriod
import java.lang.Float.max
import java.time.*
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

const val dayRoute = "day_route"

@Composable
fun DayRoute(
  onDrawerMenuClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locations = viewModel.locationsFlow.collectAsState(initial = LoadingFirst)
  val initialLocationIndex = viewModel.initialLocationIndexFlow.collectAsState(initial = 0)

  DayScreen(
    locations = locations.value,
    initialLocationIndex = initialLocationIndex.value,
    sunriseSunsetChangeInLocationAt = viewModel::sunriseSunsetChangeInLocationAt,
    currentTimeInLocationAt = viewModel::currentTimeInLocationAt,
    onDrawerMenuClick = onDrawerMenuClick,
    onAddLocationClick = onAddLocationClick,
    onEditLocationClick = onEditLocationClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayScreen(
  locations: Loadable<List<Location>>,
  initialLocationIndex: Int,
  sunriseSunsetChangeInLocationAt: (Int) -> Flow<StableLoadable<LocationSunriseSunsetChange>>,
  currentTimeInLocationAt: (Int) -> Flow<LocalTime>,
  onDrawerMenuClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  ConstraintLayout(modifier = modifier) {
    val orientation = LocalConfiguration.current.orientation
    val (topAppBar, mainContent, navigation, dayTimeCard, editLocationButton) = createRefs()
    var appBarHeightPx by remember { mutableFloatStateOf(0f) }

    val pagerState =
      rememberPagerState(initialPage = initialLocationIndex) {
        when (locations) {
          is WithData -> locations.data.size
          is WithoutData -> 0
        }
      }

    var pageChanged by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage) { if (pagerState.currentPage > 0) pageChanged = true }
    LaunchedEffect(initialLocationIndex) {
      if (!pageChanged) pagerState.animateScrollToPage(initialLocationIndex)
    }

    var dayMode by
      rememberSaveable(locations, initialLocationIndex) {
        mutableStateOf(
          when (locations) {
            is WithData -> {
              currentDayMode(
                locations.data[if (pageChanged) pagerState.currentPage else initialLocationIndex]
                  .zoneId
              )
            }
            is WithoutData -> {
              DayMode.SUNRISE
            }
          }
        )
      }
    LaunchedEffect(pagerState.currentPage) {
      dayMode =
        when (locations) {
          is WithData -> currentDayMode(locations.data[pagerState.currentPage].zoneId)
          is WithoutData -> DayMode.SUNRISE
        }
    }

    var currentChange by remember {
      mutableStateOf(StableLoadable<LocationSunriseSunsetChange>(Empty))
    }
    LaunchedEffect(pagerState.currentPage) {
      sunriseSunsetChangeInLocationAt(pagerState.currentPage).collectLatest { currentChange = it }
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
      Crossfade(targetState = locations is WithData, modifier = Modifier.fillMaxSize()) {
        pagerVisible ->
        if (pagerVisible) {
          HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            modifier = Modifier.fillMaxSize()
          ) { locationIndex ->
            val pageChange =
              sunriseSunsetChangeInLocationAt(locationIndex)
                .collectAsState(initial = StableLoadable<LocationSunriseSunsetChange>(Empty))
            val now =
              currentTimeInLocationAt(locationIndex)
                .collectAsState(
                  initial =
                    LocalTime.now(
                      if (locations is WithData) locations.data[locationIndex].zoneId
                      else ZoneId.systemDefault()
                    )
                )

            SunriseSunsetChart(
              change = pageChange.value,
              dayMode = dayMode,
              now = now.value,
              appBarHeightPx = appBarHeightPx,
              modifier = Modifier.fillMaxSize()
            )
          }

          HorizontalPagerIndicator(
            pagerState = pagerState,
            pageCount =
              when (locations) {
                is WithData -> locations.data.size
                is WithoutData -> 0
              },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            activeColor = MaterialTheme.colorScheme.onBackground,
          )
        } else {
          Box(modifier = Modifier.fillMaxSize()) {
            SunriseSunsetChart(
              change = Empty.asStable(),
              dayMode = dayMode,
              now = LocalTime.now(),
              appBarHeightPx = appBarHeightPx,
              modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
              visible = locations is Loading,
              modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(
              visible = locations is Empty,
              modifier = Modifier.align(Alignment.Center).padding(20.dp)
            ) {
              InfoButtonCard(
                infoText = stringResource(commonR.string.no_saved_locations_add_one),
                actionText = stringResource(commonR.string.add_location),
                onButtonClick = onAddLocationClick,
              )
            }
          }
        }
      }
    }

    val topBarGradient =
      Brush.verticalGradient(0f to MaterialTheme.colorScheme.background, 1f to Color.Transparent)

    fun onEditLocationClick() {
      currentChange.value.takeIfInstance<Ready<LocationSunriseSunsetChange>>()?.let { (data) ->
        onEditLocationClick(data.location.id)
      }
    }

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      DayTopAppBar(
        change = currentChange,
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
        visible = locations is Ready,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier =
          Modifier.constrainAs(dayTimeCard) {
            top.linkTo(topAppBar.bottom, 5.dp)
            end.linkTo(parent.end, 16.dp)
          },
      ) {
        ClockAndDayLengthCard(
          change = currentChange,
          modifier = Modifier.widthIn(max = LocalConfiguration.current.screenWidthDp.dp / 5 * 2)
        )
      }

      NavigationBar(
        content = {
          SunriseSunsetNavigationBarContent(
            dayMode = dayMode,
            itemsEnabled = currentChange.value is Ready,
            onDayModeChange = { dayMode = it }
          )
        },
        modifier =
          Modifier.constrainAs(navigation) {
            linkTo(mainContent.bottom, parent.bottom)
            linkTo(parent.start, parent.end)
          }
      )

      AnimatedVisibility(
        visible = currentChange.value is Ready,
        modifier =
          Modifier.constrainAs(editLocationButton) {
            bottom.linkTo(navigation.top, 16.dp)
            end.linkTo(parent.end, 16.dp)
          }
      ) {
        EditLocationButton(onClick = ::onEditLocationClick)
      }
    } else {
      DayTopAppBar(
        change = currentChange,
        modifier =
          Modifier.constrainAs(topAppBar) {
              linkTo(navigation.end, dayTimeCard.start)
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
            itemsEnabled = currentChange.value is Ready,
            onDayModeChange = { dayMode = it },
            footer = {
              AnimatedVisibility(
                visible = currentChange.value is Ready,
                modifier = Modifier.padding(bottom = 8.dp)
              ) {
                EditLocationButton(onClick = ::onEditLocationClick)
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
        visible = currentChange.value is Ready,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier =
          Modifier.constrainAs(dayTimeCard) {
            top.linkTo(parent.top, 16.dp)
            end.linkTo(parent.end, 16.dp)
          },
      ) {
        ClockAndDayLengthCard(
          change = currentChange,
          modifier = Modifier.widthIn(max = LocalConfiguration.current.screenWidthDp.dp / 5 * 2)
        )
      }
    }
  }
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

@OptIn(ExperimentalFoundationApi::class)
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
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().basicMarquee().padding(horizontal = 10.dp)
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
    remember(change) { dayPeriodFlow(change.value) }.collectAsState(initial = DayPeriod.DAY)

  Surface(
    shape = CardDefaults.shape,
    color = dayPeriod.value.color(),
    shadowElevation = 6.dp,
    modifier = modifier
  ) {
    change.value.takeIfInstance<WithData<LocationSunriseSunsetChange>>()?.let {
      val (location, today, _) = it.data
      if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(8.dp)
        ) {
          Clock(zoneId = location.zoneId, dayPeriod = dayPeriod.value)
          NowTimezoneDiffText(zoneId = location.zoneId, dayPeriod = dayPeriod.value)
          Spacer(modifier = Modifier.height(5.dp))
          DayLengthInfo(change = it.data, dayPeriod = dayPeriod.value)
          Spacer(modifier = Modifier.height(5.dp))
          NextDayPeriodTimer(
            dayPeriod = dayPeriod.value,
            dayMode = currentDayMode(location.zoneId),
            today = today,
            zoneId = location.zoneId
          )
        }
      } else {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Clock(zoneId = location.zoneId, dayPeriod = dayPeriod.value)
              NowTimezoneDiffText(zoneId = location.zoneId, dayPeriod = dayPeriod.value)
            }
            Spacer(modifier = Modifier.width(5.dp))
            DayLengthInfo(change = it.data, dayPeriod = dayPeriod.value)
          }
          Spacer(modifier = Modifier.height(5.dp))
          NextDayPeriodTimer(
            dayPeriod = dayPeriod.value,
            dayMode = currentDayMode(location.zoneId),
            today = today,
            zoneId = location.zoneId
          )
        }
      }
    }
  }
}

private data class NextDayPeriod(val timestamp: LocalTime, val label: String)

@Composable
private fun NextDayPeriodTimer(
  dayPeriod: DayPeriod,
  dayMode: DayMode,
  today: SunriseSunset,
  zoneId: ZoneId
) {
  val nextPeriod = rememberNextDayPeriod(dayPeriod, dayMode, today)
  val to = stringResource(id = R.string.to)

  AnimatedVisibility(visible = nextPeriod != null, enter = fadeIn(), exit = fadeOut()) {
    var timerText by rememberSaveable {
      mutableStateOf(
        "${nextPeriod?.timestamp?.formatTimeUntilNow(zoneId) ?: ""} $to ${nextPeriod?.label ?: ""}"
      )
    }

    nextPeriod?.let {
      LaunchedEffect(dayPeriod, today) {
        flow {
            delay(System.currentTimeMillis() % 1_000L)
            while (currentCoroutineContext().isActive) {
              emit("${it.timestamp.formatTimeUntilNow(zoneId)} $to ${nextPeriod.label}")
              delay(1_000L)
            }
          }
          .collect { timerText = it }
      }
    }

    Text(
      text = timerText,
      style =
        MaterialTheme.typography.bodySmall.copy(
          color = dayPeriod.textColor(),
          shadow =
            Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f),
          fontSize = 14.sp,
        ),
      textAlign = TextAlign.Center,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun rememberNextDayPeriod(
  dayPeriod: DayPeriod,
  dayMode: DayMode,
  today: SunriseSunset,
): NextDayPeriod? {
  val astronomicalDawn = stringResource(id = R.string.astronomical_dawn).lowercase()
  val nauticalDawn = stringResource(id = R.string.nautical_dawn).lowercase()
  val civilDawn = stringResource(id = R.string.civil_dawn).lowercase()
  val sunrise = stringResource(id = R.string.sunrise).lowercase()
  val sunset = stringResource(id = R.string.sunset).lowercase()
  val civilDusk = stringResource(id = R.string.civil_dusk).lowercase()
  val nauticalDusk = stringResource(id = R.string.nautical_dusk).lowercase()
  val astronomicalDusk = stringResource(id = R.string.astronomical_dusk).lowercase()
  return remember(dayPeriod, today) {
    when (dayPeriod) {
      DayPeriod.NIGHT -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.astronomicalTwilightBegin?.let {
              NextDayPeriod(it.toLocalTime(), astronomicalDawn)
            }
          }
          DayMode.SUNSET -> {
            null
          }
        }
      }
      DayPeriod.ASTRONOMICAL -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.nauticalTwilightBegin?.let { NextDayPeriod(it.toLocalTime(), nauticalDawn) }
              ?: today.astronomicalTwilightEnd?.let {
                NextDayPeriod(it.toLocalTime(), astronomicalDusk)
              }
          }
          DayMode.SUNSET -> {
            today.astronomicalTwilightEnd?.let { NextDayPeriod(it.toLocalTime(), astronomicalDusk) }
          }
        }
      }
      DayPeriod.NAUTICAL -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.civilTwilightBegin?.let { NextDayPeriod(it.toLocalTime(), civilDawn) }
              ?: today.nauticalTwilightEnd?.let { NextDayPeriod(it.toLocalTime(), nauticalDusk) }
          }
          DayMode.SUNSET -> {
            today.nauticalTwilightEnd?.let { NextDayPeriod(it.toLocalTime(), nauticalDusk) }
          }
        }
      }
      DayPeriod.CIVIL -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.sunrise?.let { NextDayPeriod(it.toLocalTime(), sunrise) }
              ?: today.civilTwilightEnd?.let { NextDayPeriod(it.toLocalTime(), civilDusk) }
          }
          DayMode.SUNSET -> {
            today.civilTwilightEnd?.let { NextDayPeriod(it.toLocalTime(), civilDusk) }
          }
        }
      }
      DayPeriod.DAY -> today.sunset?.let { NextDayPeriod(it.toLocalTime(), sunset) }
    }
  }
}

private fun LocalTime.formatTimeUntilNow(zoneId: ZoneId) =
  formatTimeMillis(millis = (toSecondOfDay() - LocalTime.now(zoneId).toSecondOfDay()) * 1_000L)

private fun dayPeriodFlow(change: Loadable<LocationSunriseSunsetChange>): Flow<DayPeriod> = flow {
  while (currentCoroutineContext().isActive) {
    emit(
      change.map { (location, today) -> today.currentPeriodIn(location) }.dataOrElse(DayPeriod.DAY)
    )
    delay(1_000L)
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
private fun DayLengthInfo(change: LocationSunriseSunsetChange, dayPeriod: DayPeriod) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
    DayLengthIcon(dayPeriod = dayPeriod)
    Spacer(modifier = Modifier.width(5.dp))
    Column(horizontalAlignment = Alignment.End) {
      val (location, today, yesterday) = change
      val todayLengthSeconds = today.dayLengthSecondsAtLocation(location)
      val yesterdayLengthSeconds = yesterday.dayLengthSecondsAtLocation(location)
      val dayLengthDiffTime = dayLengthDiffTime(todayLengthSeconds, yesterdayLengthSeconds)
      val diffPrefix =
        dayLengthDiffPrefix(
          todayLengthSeconds = todayLengthSeconds,
          yesterdayLengthSeconds = yesterdayLengthSeconds
        )

      Text(
        text = formatTimeMillis(todayLengthSeconds * 1_000L),
        color = dayPeriod.textColor(),
        style =
          MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            shadow =
              Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
          )
      )
      Text(
        text = formatTimeDifference(diffPrefix, dayLengthDiffTime),
        color =
          when (diffPrefix) {
            "+" -> Color.Green
            "-" -> Color.Red
            else -> dayPeriod.textColor()
          },
        style =
          MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            shadow =
              Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
          )
      )
    }
  }
}

@Composable
private fun DayLengthIcon(dayPeriod: DayPeriod) {
  Box {
    Icon(
      painter = painterResource(id = commonR.drawable.day_length_shadow),
      tint = Color.Unspecified,
      contentDescription = null,
      modifier = Modifier.offset(x = 1.dp, y = 1.dp)
    )

    Icon(
      painter =
        painterResource(
          id =
            if (dayPeriod == DayPeriod.DAY) commonR.drawable.day_length_black
            else commonR.drawable.day_length_white
        ),
      tint = Color.Unspecified,
      contentDescription = stringResource(R.string.day_length)
    )
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

private fun currentDayMode(zoneId: ZoneId): DayMode =
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
  val civilDawnLabel = stringResource(id = R.string.civil_dawn_degrees_below, edgeLabelSeparator)
  val civilDuskLabel = stringResource(id = R.string.civil_dusk_degrees_below, edgeLabelSeparator)
  val nauticalDawnLabel =
    stringResource(id = R.string.nautical_dawn_degrees_below, edgeLabelSeparator)
  val nauticalDuskLabel =
    stringResource(id = R.string.nautical_dusk_degrees_below, edgeLabelSeparator)
  val astronomicalDawnLabel =
    stringResource(id = R.string.astronomical_dawn_degrees_below, edgeLabelSeparator)
  val astronomicalDuskLabel =
    stringResource(id = R.string.astronomical_dusk_degrees_below, edgeLabelSeparator)

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
