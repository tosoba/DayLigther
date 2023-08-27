package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.domain.model.WithoutData
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
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
  fun givenWithoutDataLocations_emptyChartIsShown() {
    with(composeTestRule) {
      setContent {
        TestDayScreen(modifier = Modifier.fillMaxSize(), locations = mockk<WithoutData>())
      }
      onNodeWithEnumTestTag(DayTestTags.WITHOUT_DATA_LOCATIONS_CHART).assertIsDisplayed()
    }
  }

  @Test
  fun givenEmptyLocations_noSavedLocationsInfoButtonCardIsShown() {
    with(composeTestRule) {
      setContent { TestDayScreen(modifier = Modifier.fillMaxSize()) }
      onNodeWithEnumTestTag(DayTestTags.EMPTY_LOCATIONS_CARD).assertIsDisplayed()
      onNodeWithText(activity.getString(R.string.no_saved_locations)).assertIsDisplayed()
      onNodeWithText(activity.getString(R.string.add_location)).assertIsDisplayed()
    }
  }
}
