package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.uitesthiltmanifest.HiltComponentActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DayScreenTests {
  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

  @Before fun setup() = hiltRule.inject()

  @Test
  fun givenEmptyLocations_noSavedLocationsInfoButtonCardIsShown() {
    with(composeTestRule) {
      setContent { TestDayScreen(modifier = Modifier.fillMaxSize()) }
      onNodeWithEnumTestTag(DayTestTags.NO_LOCATIONS_CARD).assertIsDisplayed()
      onNodeWithText(activity.getString(R.string.no_saved_locations)).assertIsDisplayed()
      onNodeWithText(activity.getString(R.string.add_location)).assertIsDisplayed()
    }
  }

  @Composable
  private fun TestDayScreen(
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
    modifier: Modifier = Modifier,
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
}
