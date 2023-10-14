package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.WithoutData
import com.trm.daylighter.core.testing.model.testLocation
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.core.testing.util.setContentHarness
import com.trm.daylighter.core.testing.util.testDpSize
import com.trm.daylighter.uitesthiltmanifest.HiltComponentActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DayScreenTests {
  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

  @Before fun setup() = hiltRule.inject()

  @Test
  fun givenWithoutDataLocations_emptyChartIsDisplayed() {
    with(composeTestRule) {
      setContent {
        TestDayScreen(modifier = Modifier.fillMaxSize(), locations = mockk<WithoutData>())
      }
      onNodeWithEnumTestTag(DayTestTags.WITHOUT_DATA_LOCATIONS_CHART).assertIsDisplayed()
    }
  }

  @Test
  fun givenLoadingLocations_progressIndicatorIsDisplayed() {
    with(composeTestRule) {
      setContent { TestDayScreen(modifier = Modifier.fillMaxSize(), locations = LoadingFirst) }
      onNodeWithEnumTestTag(DayTestTags.LOADING_LOCATIONS_PROGRESS_INDICATOR).assertIsDisplayed()
    }
  }

  @Test
  fun givenEmptyLocations_noSavedLocationsInfoButtonCardIsDisplayed() {
    with(composeTestRule) {
      setContent { TestDayScreen(modifier = Modifier.fillMaxSize()) }
      onNodeWithEnumTestTag(DayTestTags.EMPTY_LOCATIONS_CARD).assertIsDisplayed()
      onNodeWithText(activity.getString(R.string.no_saved_locations)).assertIsDisplayed()
      onNodeWithText(activity.getString(R.string.add_location)).assertIsDisplayed()
    }
  }

  @Test
  fun whenWidthIsNotCompactAndUsingNavigationBar_usingMaxWidthTopAppBar() {
    with(composeTestRule) {
      val size = testDpSize(width = TestWidthClass.COMPACT, height = TestHeightClass.MEDIUM)
      setContentHarness(size) {
        TestDayScreen(modifier = Modifier.fillMaxSize(), locations = Ready(listOf(testLocation())))
      }
      onNodeWithEnumTestTag(DayTestTags.TOP_APP_BAR).assertWidthIsEqualTo(size.width)
    }
  }
}
