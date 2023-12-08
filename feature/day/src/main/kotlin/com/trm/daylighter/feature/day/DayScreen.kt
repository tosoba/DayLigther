package com.trm.daylighter.feature.day

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.model.DayMode
import com.trm.daylighter.core.common.model.DayPeriod
import com.trm.daylighter.core.common.util.ext.*
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.util.ext.dayLengthSecondsAtLocation
import com.trm.daylighter.core.ui.composable.*
import com.trm.daylighter.core.ui.local.LocalHeightSizeClass
import com.trm.daylighter.core.ui.local.LocalWidthSizeClass
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.core.ui.theme.*
import com.trm.daylighter.core.ui.util.enumTestTag
import com.trm.daylighter.core.ui.util.ext.color
import com.trm.daylighter.core.ui.util.ext.textColor
import com.trm.daylighter.core.ui.util.ext.textShadowColor
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer
import java.time.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

const val dayNightCycleRoute = "day_night_cycle_route"
const val goldenBlueHourRoute = "golden_blue_hour_route"

@Composable
fun DayRoute(
  chartMode: DayPeriodChartMode,
  onDrawerMenuClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onEditLocationClick: (Long) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locations = viewModel.locationsFlow.collectAsStateWithLifecycle(initialValue = LoadingFirst)
  val initialLocationIndex =
    viewModel.initialLocationIndexFlow.collectAsStateWithLifecycle(initialValue = 0)

  DayScreen(
    chartMode = chartMode,
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

private val usingNavigationBar: Boolean
  @Composable
  get() =
    LocalWidthSizeClass.current == WindowWidthSizeClass.Compact ||
      LocalHeightSizeClass.current == WindowHeightSizeClass.Expanded

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DayScreen(
  chartMode: DayPeriodChartMode,
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
    var now by remember {
      mutableStateOf(
        LocalTime.now(
          if (locations is WithData) locations.data[pagerState.currentPage].zoneId
          else ZoneId.systemDefault()
        )
      )
    }

    LaunchedEffect(pagerState.currentPage) {
      sunriseSunsetChangeInLocationAt(pagerState.currentPage).collectLatest { currentChange = it }
    }
    LaunchedEffect(pagerState.currentPage) {
      currentTimeInLocationAt(pagerState.currentPage).collectLatest { now = it }
    }

    val usingNavigationBar = usingNavigationBar
    Box(
      modifier =
        Modifier.constrainAs(mainContent) {
          if (usingNavigationBar) {
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
      Crossfade(
        targetState = locations is WithData,
        modifier = Modifier.fillMaxSize(),
        label = "chart-crossfade"
      ) { pagerVisible ->
        if (pagerVisible) {
          HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            modifier = Modifier.fillMaxSize()
          ) {
            DayPeriodChart(
              change = currentChange,
              modifier = Modifier.fillMaxSize(),
              chartMode = chartMode,
              dayMode = dayMode,
              now = now,
              appBarHeightPx = appBarHeightPx
            )
          }

          Row(
            modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
          ) {
            repeat(
              when (locations) {
                is WithData -> locations.data.size
                is WithoutData -> 0
              }
            ) {
              Box(
                modifier =
                  Modifier.padding(2.dp)
                    .clip(CircleShape)
                    .background(
                      LocalContentColor.current.copy(
                        alpha =
                          if (pagerState.currentPage == it) LocalContentAlpha.current
                          else ContentAlpha.disabled
                      )
                    )
                    .size(10.dp)
              )
            }
          }
        } else {
          Box(modifier = Modifier.fillMaxSize()) {
            DayPeriodChart(
              change = Empty.asStable(),
              modifier =
                Modifier.fillMaxSize()
                  .alpha(.15f)
                  .enumTestTag(DayTestTags.WITHOUT_DATA_LOCATIONS_CHART),
              chartMode = chartMode,
              appBarHeightPx = appBarHeightPx
            )

            AnimatedVisibility(
              visible = locations is Loading,
              enter = fadeIn(),
              exit = fadeOut(),
              modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
              LinearProgressIndicator(
                modifier =
                  Modifier.fillMaxWidth()
                    .enumTestTag(DayTestTags.LOADING_LOCATIONS_PROGRESS_INDICATOR)
              )
            }

            AnimatedVisibility(
              visible = locations is Empty,
              enter = fadeIn(),
              exit = fadeOut(),
              modifier = Modifier.align(Alignment.Center).padding(20.dp)
            ) {
              InfoButtonCard(
                infoText = stringResource(commonR.string.no_saved_locations),
                actionText = stringResource(commonR.string.add_location),
                onButtonClick = onAddLocationClick,
                modifier = Modifier.enumTestTag(DayTestTags.EMPTY_LOCATIONS_CARD)
              )
            }
          }
        }
      }
    }

    var trailingSpacerWidthPx: Int? by remember { mutableStateOf(0) }
    val showNavigationIcon = !usingPermanentNavigationDrawer && usingNavigationBar
    DayTopAppBar(
      change = currentChange,
      chartMode = chartMode,
      colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
      navigationIcon = {
        if (showNavigationIcon) {
          DrawerMenuIconButton(
            onClick = onDrawerMenuClick,
            modifier = Modifier.enumTestTag(DayTestTags.DRAWER_MENU_ICON_BUTTON)
          )
        }
      },
      leading = { Spacer(modifier = Modifier.width(if (showNavigationIcon) 15.dp else 5.dp)) },
      trailing = {
        Spacer(
          modifier =
            Modifier.width(
              trailingSpacerWidthPx?.let { with(LocalDensity.current) { it.toDp() } } ?: 0.dp
            )
        )
      },
      modifier =
        Modifier.constrainAs(topAppBar) {
            linkTo(
              start = if (usingNavigationBar) mainContent.start else navigation.end,
              end = mainContent.end
            )
            top.linkTo(parent.top)
            width = Dimension.fillToConstraints
          }
          .background(backgroundToTransparentVerticalGradient)
          .onGloballyPositioned { coordinates ->
            appBarHeightPx = coordinates.size.height.toFloat()
          }
          .enumTestTag(DayTestTags.TOP_APP_BAR)
    )

    fun onEditLocationClick() {
      currentChange.value.takeIfInstance<Ready<LocationSunriseSunsetChange>>()?.let { (data) ->
        onEditLocationClick(data.location.id)
      }
    }

    if (usingNavigationBar) {
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
            .enumTestTag(DayTestTags.NAVIGATION_BAR)
      )
    } else {
      NavigationRail(
        header = {
          if (!usingPermanentNavigationDrawer) {
            DrawerMenuIconButton(
              onClick = onDrawerMenuClick,
              modifier = Modifier.enumTestTag(DayTestTags.DRAWER_MENU_ICON_BUTTON)
            )
          }
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
                EditLocationButton(
                  onClick = ::onEditLocationClick,
                  modifier = Modifier.enumTestTag(DayTestTags.EDIT_LOCATION_BUTTON)
                )
              }
            }
          )
        },
        modifier =
          Modifier.constrainAs(navigation) {
              start.linkTo(parent.start)
              linkTo(parent.top, parent.bottom)
            }
            .enumTestTag(DayTestTags.NAVIGATION_RAIL),
      )
    }

    val constrainClockAndDayLengthCardToAppBarBottom =
      LocalHeightSizeClass.current != WindowHeightSizeClass.Compact
    AnimatedVisibility(
      visible = locations is Ready,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier =
        Modifier.constrainAs(dayTimeCard) {
          top.run {
            if (constrainClockAndDayLengthCardToAppBarBottom) linkTo(topAppBar.bottom, 5.dp)
            else linkTo(parent.top, 16.dp)
          }
          end.linkTo(parent.end, 16.dp)
        },
    ) {
      ClockAndDayLengthCard(
        change = currentChange,
        chartMode = chartMode,
        modifier =
          Modifier.widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.4f).run {
            if (!constrainClockAndDayLengthCardToAppBarBottom) {
              onGloballyPositioned { trailingSpacerWidthPx = it.size.width }
            } else {
              this
            }
          }
      )
    }

    if (usingNavigationBar) {
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
        EditLocationButton(
          onClick = ::onEditLocationClick,
          modifier = Modifier.enumTestTag(DayTestTags.EDIT_LOCATION_BUTTON)
        )
      }
    }
  }
}

@Composable
private fun EditLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  FloatingActionButton(onClick = onClick, modifier = modifier) {
    Icon(
      imageVector = Icons.Filled.Edit,
      contentDescription = stringResource(R.string.edit_location)
    )
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DayTopAppBar(
  change: StableLoadable<LocationSunriseSunsetChange>,
  chartMode: DayPeriodChartMode,
  modifier: Modifier = Modifier,
  colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
  navigationIcon: @Composable () -> Unit = {},
  leading: @Composable () -> Unit = {},
  trailing: @Composable () -> Unit = {}
) {
  CenterAlignedTopAppBar(
    modifier = modifier,
    colors = colors,
    navigationIcon = navigationIcon,
    title = {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        leading()
        Box(
          modifier =
            Modifier.weight(1f)
              .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
              .drawWithContent {
                drawContent()
                drawRect(
                  brush =
                    Brush.horizontalGradient(
                      0f to Color.Transparent,
                      0.05f to Color.Black,
                      0.95f to Color.Black,
                      1f to Color.Transparent
                    ),
                  blendMode = BlendMode.DstIn
                )
              }
        ) {
          DayTopAppBarTitle(
            change = change,
            chartMode = chartMode,
            modifier = Modifier.basicMarquee().align(Alignment.Center).padding(10.dp)
          )
        }
        trailing()
      }
    },
  )
}

@Composable
private fun DayTopAppBarTitle(
  change: StableLoadable<LocationSunriseSunsetChange>,
  chartMode: DayPeriodChartMode,
  modifier: Modifier,
) {
  Text(
    text =
      change.value
        .map { (location) -> location.name }
        .dataOrElse(
          stringResource(
            when (chartMode) {
              DayPeriodChartMode.DAY_NIGHT_CYCLE -> commonR.string.day_night_cycle
              DayPeriodChartMode.GOLDEN_BLUE_HOUR -> commonR.string.golden_blue_hour
            }
          )
        ),
    style = appBarTextStyle(),
    maxLines = 1,
    textAlign = TextAlign.Center,
    modifier = modifier
  )
}

@Composable
private fun ClockAndDayLengthCard(
  change: StableLoadable<LocationSunriseSunsetChange>,
  chartMode: DayPeriodChartMode,
  modifier: Modifier = Modifier,
) {
  val dayPeriod =
    remember(change, chartMode) { dayPeriodFlow(change = change.value, chartMode = chartMode) }
      .collectAsStateWithLifecycle(initialValue = DayPeriod.DAY)

  Surface(
    shape = CardDefaults.shape,
    color = dayPeriod.value.color(),
    shadowElevation = 6.dp,
    modifier = modifier
  ) {
    change.value.takeIfInstance<WithData<LocationSunriseSunsetChange>>()?.let {
      val (location, today, _) = it.data
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
      ) {
        Clock(zoneId = location.zoneId, dayPeriod = dayPeriod.value)

        NowTimezoneDiffText(zoneId = location.zoneId, dayPeriod = dayPeriod.value)

        if (chartMode == DayPeriodChartMode.DAY_NIGHT_CYCLE) {
          Spacer(modifier = Modifier.height(2.dp))

          DayLengthInfo(
            change = it.data,
            dayPeriod = dayPeriod.value,
            modifier = Modifier.wrapContentSize()
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        NextDayPeriodTimer(
          dayPeriod = dayPeriod.value,
          dayMode = location.zoneId.currentDayMode(),
          chartMode = chartMode,
          today = today,
          zoneId = location.zoneId
        )
      }
    }
  }
}

private data class NextDayPeriod(val timestamp: LocalTime, val label: String)

@Composable
private fun NextDayPeriodTimer(
  dayPeriod: DayPeriod,
  dayMode: DayMode,
  chartMode: DayPeriodChartMode,
  today: SunriseSunset,
  zoneId: ZoneId
) {
  val nextPeriod =
    rememberNextDayPeriod(
      dayPeriod = dayPeriod,
      dayMode = dayMode,
      chartMode = chartMode,
      today = today
    )
  val timerPositive =
    remember(nextPeriod) { nextPeriod != null && nextPeriod.timestamp.secondsUntilNow(zoneId) > 0 }
  val till = stringResource(R.string.till)

  AnimatedVisibility(
    visible = nextPeriod != null && timerPositive,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    var timerText by rememberSaveable {
      mutableStateOf(
        "${nextPeriod?.timestamp?.formatTimeUntilNow(zoneId) ?: ""} $till ${nextPeriod?.label ?: ""}"
      )
    }

    nextPeriod?.let {
      LaunchedEffect(dayPeriod, today) {
        flow {
            delay(System.currentTimeMillis() % 1_000L)
            while (currentCoroutineContext().isActive) {
              emit("${it.timestamp.formatTimeUntilNow(zoneId)} $till ${nextPeriod.label}")
              delay(1_000L)
            }
          }
          .collect { timerText = it }
      }
    }

    val height = LocalConfiguration.current.screenHeightDp
    Text(
      text = timerText,
      style =
        MaterialTheme.typography.bodySmall.copy(
          color = dayPeriod.textColor(),
          shadow =
            Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f),
          fontSize =
            when {
              height < 650 -> 12
              height > 1_000 -> 16
              else -> 14
            }.sp,
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
  chartMode: DayPeriodChartMode,
  today: SunriseSunset,
): NextDayPeriod? {
  val nightLabel = stringResource(commonR.string.night).lowercase()
  val astronomicalLabel = stringResource(commonR.string.astronomical_twilight).lowercase()
  val nauticalLabel = stringResource(commonR.string.nautical_twilight).lowercase()
  val civilLabel = stringResource(commonR.string.civil_twilight).lowercase()
  val dayLabel = stringResource(commonR.string.day).lowercase()
  val goldenHourLabel = stringResource(commonR.string.golden_hour).lowercase()
  val blueHourLabel = stringResource(commonR.string.blue_hour).lowercase()

  return remember(dayPeriod, today) {
    when (dayPeriod) {
      DayPeriod.NIGHT -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.morning18Below?.let { NextDayPeriod(it.toLocalTime(), astronomicalLabel) }
          }
          DayMode.SUNSET -> {
            null
          }
        }
      }
      DayPeriod.ASTRONOMICAL -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.morning12Below?.let { NextDayPeriod(it.toLocalTime(), nauticalLabel) }
          }
          DayMode.SUNSET -> {
            today.evening18Below?.let { NextDayPeriod(it.toLocalTime(), nightLabel) }
          }
        }
      }
      DayPeriod.NAUTICAL -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            when (chartMode) {
              DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
                today.morning6Below?.let { NextDayPeriod(it.toLocalTime(), civilLabel) }
              }
              DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
                today.morning6Below?.let { NextDayPeriod(it.toLocalTime(), blueHourLabel) }
              }
            }
          }
          DayMode.SUNSET -> {
            today.evening12Below?.let { NextDayPeriod(it.toLocalTime(), astronomicalLabel) }
          }
        }
      }
      DayPeriod.CIVIL -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.sunrise?.let { NextDayPeriod(it.toLocalTime(), dayLabel) }
          }
          DayMode.SUNSET -> {
            today.evening6Below?.let { NextDayPeriod(it.toLocalTime(), nauticalLabel) }
          }
        }
      }
      DayPeriod.DAY -> {
        when (chartMode) {
          DayPeriodChartMode.DAY_NIGHT_CYCLE -> {
            today.sunset?.let { NextDayPeriod(it.toLocalTime(), civilLabel) }
          }
          DayPeriodChartMode.GOLDEN_BLUE_HOUR -> {
            today.evening6Above?.let { NextDayPeriod(it.toLocalTime(), goldenHourLabel) }
          }
        }
      }
      DayPeriod.GOLDEN_HOUR -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.morning6Above?.let { NextDayPeriod(it.toLocalTime(), dayLabel) }
          }
          DayMode.SUNSET -> {
            today.evening4Below?.let { NextDayPeriod(it.toLocalTime(), blueHourLabel) }
          }
        }
      }
      DayPeriod.BLUE_HOUR -> {
        when (dayMode) {
          DayMode.SUNRISE -> {
            today.morning4Below?.let { NextDayPeriod(it.toLocalTime(), goldenHourLabel) }
          }
          DayMode.SUNSET -> {
            today.evening6Below?.let { NextDayPeriod(it.toLocalTime(), nauticalLabel) }
          }
        }
      }
    }
  }
}

private fun dayPeriodFlow(
  change: Loadable<LocationSunriseSunsetChange>,
  chartMode: DayPeriodChartMode
): Flow<DayPeriod> = flow {
  while (currentCoroutineContext().isActive) {
    emit(
      change
        .map { (location, today) ->
          today.currentPeriodIn(
            location = location,
            useGoldenBlueHour = chartMode == DayPeriodChartMode.GOLDEN_BLUE_HOUR
          )
        }
        .dataOrElse(DayPeriod.DAY)
    )
    delay(1_000L)
  }
}

@Composable
private fun Clock(zoneId: ZoneId, dayPeriod: DayPeriod, modifier: Modifier = Modifier) {
  val textStyle = MaterialTheme.typography.labelLarge
  val resolver = LocalFontFamilyResolver.current
  val height = LocalConfiguration.current.screenHeightDp

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
        setTextSize(
          TypedValue.COMPLEX_UNIT_SP,
          when {
            height < 650 -> 20f
            height > 1_000 -> 28f
            else -> 24f
          }
        )
        onZoneIdOrDayPeriodUpdate()
      }
    },
    update = { it.onZoneIdOrDayPeriodUpdate() },
    modifier = modifier
  )
}

@Composable
private fun NowTimezoneDiffText(
  zoneId: ZoneId,
  dayPeriod: DayPeriod,
  modifier: Modifier = Modifier
) {
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
      ),
    modifier = modifier
  )
}

@Composable
private fun DayLengthInfo(
  change: LocationSunriseSunsetChange,
  dayPeriod: DayPeriod,
  modifier: Modifier = Modifier
) {
  val (location, today, yesterday) = change
  val todayLengthSeconds = today.dayLengthSecondsAtLocation(location)
  val yesterdayLengthSeconds = yesterday.dayLengthSecondsAtLocation(location)
  val dayLengthDiffTime = dayLengthDiffTime(todayLengthSeconds, yesterdayLengthSeconds)
  val diffPrefix =
    dayLengthDiffPrefix(
      todayLengthSeconds = todayLengthSeconds,
      yesterdayLengthSeconds = yesterdayLengthSeconds
    )

  ConstraintLayout(modifier = modifier) {
    val (dayLengthLabelText, icon, lengthText, diffText) = createRefs()
    if (LocalHeightSizeClass.current != WindowHeightSizeClass.Compact) {
      DayLengthLabelText(
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(dayLengthLabelText) {
            top.linkTo(parent.top)
            bottom.linkTo(lengthText.top)
            start.linkTo(lengthText.start)
            end.linkTo(lengthText.end)
          }
      )

      DayLengthIcon(
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(icon) {
            top.linkTo(dayLengthLabelText.top)
            bottom.linkTo(diffText.bottom)
            start.linkTo(parent.start)
          }
      )

      DayLengthText(
        todayLengthSeconds = todayLengthSeconds,
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(lengthText) {
            top.linkTo(dayLengthLabelText.bottom)
            bottom.linkTo(diffText.top)
            start.linkTo(icon.end, 5.dp)
            end.linkTo(parent.end)
          }
      )

      DayLengthDiffText(
        diffPrefix = diffPrefix,
        dayLengthDiffTime = dayLengthDiffTime,
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(diffText) {
            width = Dimension.wrapContent
            top.linkTo(lengthText.bottom)
            bottom.linkTo(parent.bottom)
            end.linkTo(parent.end)
          }
      )
    } else {
      DayLengthLabelText(
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(dayLengthLabelText) {
            top.linkTo(parent.top)
            bottom.linkTo(lengthText.top)
            start.linkTo(lengthText.start)
            end.linkTo(diffText.end)
          }
      )

      DayLengthIcon(
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(icon) {
            height = Dimension.fillToConstraints
            width = Dimension.ratio("1:1")
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(lengthText.start)
          }
      )

      DayLengthText(
        todayLengthSeconds = todayLengthSeconds,
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(lengthText) {
            top.linkTo(dayLengthLabelText.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(icon.end, 5.dp)
            end.linkTo(diffText.end)
          }
      )

      DayLengthDiffText(
        diffPrefix = diffPrefix,
        dayLengthDiffTime = dayLengthDiffTime,
        dayPeriod = dayPeriod,
        modifier =
          Modifier.constrainAs(diffText) {
            top.linkTo(dayLengthLabelText.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(lengthText.end, 5.dp)
            end.linkTo(parent.end)
          }
      )
    }
  }
}

@Composable
private fun DayLengthLabelText(dayPeriod: DayPeriod, modifier: Modifier = Modifier) {
  Text(
    text = "${stringResource(R.string.day_length)}:",
    textAlign = TextAlign.Center,
    overflow = TextOverflow.Ellipsis,
    color = dayPeriod.textColor(),
    style =
      MaterialTheme.typography.bodySmall.copy(
        shadow =
          Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
      ),
    modifier = modifier
  )
}

@Composable
private fun DayLengthIcon(dayPeriod: DayPeriod, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    Icon(
      painter = painterResource(commonR.drawable.day_length_shadow),
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
private fun DayLengthText(
  todayLengthSeconds: Long,
  dayPeriod: DayPeriod,
  modifier: Modifier = Modifier
) {
  val height = LocalConfiguration.current.screenHeightDp
  Text(
    text = formatTimeMillis(todayLengthSeconds * 1_000L),
    color = dayPeriod.textColor(),
    style =
      MaterialTheme.typography.bodyLarge.copy(
        fontSize =
          when {
            height < 650 -> 15
            height > 1_000 -> 20
            else -> 18
          }.sp,
        shadow =
          Shadow(color = dayPeriod.textShadowColor(), offset = Offset(1f, 1f), blurRadius = 1f)
      ),
    modifier = modifier,
  )
}

@Composable
private fun DayLengthDiffText(
  diffPrefix: String,
  dayLengthDiffTime: LocalTime,
  dayPeriod: DayPeriod,
  modifier: Modifier = Modifier
) {
  val height = LocalConfiguration.current.screenHeightDp
  Text(
    text = formatTimeDifference(prefix = diffPrefix, diff = dayLengthDiffTime),
    color =
      when (diffPrefix) {
        "+" -> Color.Green
        "-" -> Color.Red
        else -> dayPeriod.textColor()
      },
    style =
      MaterialTheme.typography.bodyLarge.copy(
        fontSize =
          when {
            height < 650 -> 15
            height > 1_000 -> 20
            else -> 18
          }.sp,
        shadow =
          Shadow(
            color =
              if (diffPrefix == "+" || diffPrefix == "-") Color.Black
              else dayPeriod.textShadowColor(),
            offset = Offset(1f, 1f),
            blurRadius = 1f
          )
      ),
    modifier = modifier
  )
}

@Composable
private fun RowScope.SunriseSunsetNavigationBarContent(
  itemsEnabled: Boolean,
  dayMode: DayMode,
  onDayModeChange: (DayMode) -> Unit
) {
  NavigationBarItem(
    modifier = Modifier.enumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM),
    selected = itemsEnabled && dayMode == DayMode.SUNRISE,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNRISE) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunrise),
        contentDescription = stringResource(commonR.string.sunrise),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunrise),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    }
  )
  NavigationBarItem(
    modifier = Modifier.enumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM),
    selected = itemsEnabled && dayMode == DayMode.SUNSET,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNSET) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunset),
        contentDescription = stringResource(commonR.string.sunset),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunset),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
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
    modifier = Modifier.enumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM),
    selected = itemsEnabled && dayMode == DayMode.SUNRISE,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNRISE) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunrise),
        contentDescription = stringResource(commonR.string.sunrise),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunrise),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    }
  )

  NavigationRailItem(
    modifier = Modifier.enumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM),
    selected = itemsEnabled && dayMode == DayMode.SUNSET,
    enabled = itemsEnabled,
    onClick = { onDayModeChange(DayMode.SUNSET) },
    icon = {
      Icon(
        painter = painterResource(R.drawable.sunset),
        contentDescription = stringResource(commonR.string.sunset),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    },
    label = {
      Text(
        text = stringResource(commonR.string.sunset),
        modifier = Modifier.navigationItemEnabledAlpha(itemsEnabled)
      )
    }
  )

  Spacer(modifier = Modifier.weight(1f))

  footer()
}

private fun Modifier.navigationItemEnabledAlpha(itemsEnabled: Boolean) =
  this then alpha(if (!itemsEnabled) .5f else 1f)
