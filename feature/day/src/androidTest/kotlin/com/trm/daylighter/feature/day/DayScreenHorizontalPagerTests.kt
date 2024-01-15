package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.testing.model.testLocation
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.feature.day.composable.TestDayScreen
import org.junit.Rule
import org.junit.Test

class DayScreenHorizontalPagerTests {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun givenReadyLocations_horizontalPagerIsDisplayed() {
    with(composeTestRule) {
      setContent {
        TestDayScreen(modifier = Modifier.fillMaxSize(), locations = Ready(listOf(testLocation())))
      }
      onNodeWithEnumTestTag(DayTestTags.LOCATIONS_CHART_PAGER).assertIsDisplayed()
    }
  }
}
