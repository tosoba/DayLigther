package com.trm.daylighter.feature.day.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.feature.day.DayScreen
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
  onAddLocationClick: () -> Unit = {},
  onEditLocationClick: (Long) -> Unit = {},
) {
  DayScreen(
    chartMode = chartMode,
    locations = locations,
    initialLocationIndex = initialLocationIndex,
    sunriseSunsetChangeInLocationAt = sunriseSunsetChangeInLocationAt,
    currentTimeInLocationAt = currentTimeInLocationAt,
    onDrawerMenuClick = onDrawerMenuClick,
    onAddLocationClick = onAddLocationClick,
    onEditLocationClick = onEditLocationClick,
    modifier = modifier
  )
}

internal fun isUsingNavigationBar(items: Array<Any>): Boolean =
  items[0] == TestWidthClass.COMPACT || items[1] == TestHeightClass.EXPANDED
