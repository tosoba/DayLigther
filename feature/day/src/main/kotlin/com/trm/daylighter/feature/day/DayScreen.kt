package com.trm.daylighter.feature.day

import android.content.res.Configuration
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.widget.TextClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
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
import com.trm.daylighter.core.common.model.DayMode
import com.trm.daylighter.core.common.model.DayPeriod
import com.trm.daylighter.core.common.util.ext.*
import com.trm.daylighter.core.common.util.ext.currentPeriodIn
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.util.ext.dayLengthSecondsAtLocation
import com.trm.daylighter.core.ui.composable.*
import com.trm.daylighter.core.ui.composable.DayPeriodChart
import com.trm.daylighter.core.ui.ext.color
import com.trm.daylighter.core.ui.ext.textColor
import com.trm.daylighter.core.ui.ext.textShadowColor
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.*
import java.time.*
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
              locations.data[if (pageChanged) pagerState.currentPage else initialLocationIndex]
                .zoneId
                .currentDayMode()
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
          is WithData -> locations.data[pagerState.currentPage].zoneId.currentDayMode()
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

            DayPeriodChart(
              change = pageChange.value,
              modifier = Modifier.fillMaxSize(),
              dayMode = dayMode,
              now = now.value,
              appBarHeightPx = appBarHeightPx
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
            DayPeriodChart(
              change = Empty.asStable(),
              modifier = Modifier.fillMaxSize().alpha(.5f),
              appBarHeightPx = appBarHeightPx
            )

            AnimatedVisibility(
              visible = locations is Loading,
              enter = fadeIn(),
              exit = fadeOut(),
              modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(
              visible = locations is Empty,
              enter = fadeIn(),
              exit = fadeOut(),
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
            .background(backgroundToTransparentVerticalGradient)
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
        enter = fadeIn(),
        exit = fadeOut(),
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
            .background(backgroundToTransparentVerticalGradient)
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
                enter = fadeIn(),
                exit = fadeOut(),
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
            dayMode = location.zoneId.currentDayMode(),
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
            dayMode = location.zoneId.currentDayMode(),
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
  val timerPositive =
    remember(nextPeriod) { nextPeriod != null && nextPeriod.timestamp.secondsUntilNow(zoneId) > 0 }
  val to = stringResource(R.string.to)

  AnimatedVisibility(
    visible = nextPeriod != null && timerPositive,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
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
  val astronomicalDawn = stringResource(commonR.string.astronomical_dawn).lowercase()
  val nauticalDawn = stringResource(commonR.string.nautical_dawn).lowercase()
  val civilDawn = stringResource(commonR.string.civil_dawn).lowercase()
  val sunrise = stringResource(commonR.string.sunrise).lowercase()
  val sunset = stringResource(commonR.string.sunset).lowercase()
  val civilDusk = stringResource(commonR.string.civil_dusk).lowercase()
  val nauticalDusk = stringResource(commonR.string.nautical_dusk).lowercase()
  val astronomicalDusk = stringResource(commonR.string.astronomical_dusk).lowercase()
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
        contentDescription = stringResource(commonR.string.sunrise),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunrise),
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
        contentDescription = stringResource(commonR.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunset),
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
        contentDescription = stringResource(commonR.string.sunrise),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunrise),
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
        contentDescription = stringResource(commonR.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunset),
        modifier = Modifier.alpha(if (!itemsEnabled) .5f else 1f)
      )
    }
  )

  Spacer(modifier = Modifier.weight(1f))

  footer()
}
