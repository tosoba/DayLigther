package com.trm.daylighter.feature.day.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.testing.model.testLocation
import com.trm.daylighter.core.testing.model.testSunriseSunset
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.feature.day.DayScreen
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

@Suppress("TestFunctionName")
@Composable
internal fun TestDayScreen(
  modifier: Modifier = Modifier,
  chartMode: DayPeriodChartMode = DayPeriodChartMode.DAY_NIGHT_CYCLE,
  locations: Loadable<List<Location>> = Empty,
  initialLocationIndex: Int = 0,
  sunriseSunsetChangeInLocationAt: (Int) -> Flow<StableLoadable<LocationSunriseSunsetChange>> = {
    emptyFlow()
  },
  currentTimeInLocationAt: (Int) -> Flow<LocalTime> = { emptyFlow() },
  onDrawerMenuClick: () -> Unit = {},
  onNewLocationClick: () -> Unit = {},
  onEditLocationClick: (Long) -> Unit = {},
) {
  DayScreen(
    chartMode = chartMode,
    locations = locations,
    initialLocationIndex = initialLocationIndex,
    sunriseSunsetChangeInLocationAt = sunriseSunsetChangeInLocationAt,
    currentTimeInLocationAt = currentTimeInLocationAt,
    onDrawerMenuClick = onDrawerMenuClick,
    onNewLocationClick = onNewLocationClick,
    onEditLocationClick = onEditLocationClick,
    modifier = modifier,
  )
}

@Suppress("TestFunctionName")
@Composable
internal fun ReadyChangeTestDayScreen(
  location: Location = testLocation(),
  sunriseSunset: SunriseSunset = testSunriseSunset(),
) {
  TestDayScreen(
    modifier = Modifier.fillMaxSize(),
    locations = Ready(listOf(location)),
    sunriseSunsetChangeInLocationAt = {
      flowOf(
        Ready(
            LocationSunriseSunsetChange(
              location = location,
              today = sunriseSunset,
              yesterday = sunriseSunset,
            )
          )
          .asStable()
      )
    },
  )
}

internal fun isUsingNavigationBar(items: Array<Any>): Boolean =
  items[0] == TestWidthClass.COMPACT || items[1] == TestHeightClass.EXPANDED
