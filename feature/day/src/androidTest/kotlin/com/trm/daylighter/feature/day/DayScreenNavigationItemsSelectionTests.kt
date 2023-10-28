package com.trm.daylighter.feature.day

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.feature.day.composable.ReadyChangeTestDayScreen
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import java.time.LocalTime
import java.time.ZoneId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DayScreenNavigationItemsSelectionTests {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun before() {
    mockkStatic(LocalTime::class)
  }

  @After
  fun after() {
    unmockkObject(LocalTime::class)
  }

  @Test
  fun givenReadyChangeAndLocalTimeEarlierThanNoon_thenSunriseItemIsSelected() {
    every { LocalTime.now(any<ZoneId>()) } returns LocalTime.NOON.minusHours(1L)

    with(composeTestRule) {
      setContent { ReadyChangeTestDayScreen() }
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).assertIsSelected()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).assertIsNotSelected()
    }
  }

  @Test
  fun givenReadyChangeAndLocalTimeLaterThanNoon_thenSunsetItemIsSelected() {
    every { LocalTime.now(any<ZoneId>()) } returns LocalTime.NOON.plusHours(1L)

    with(composeTestRule) {
      setContent { ReadyChangeTestDayScreen() }
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).assertIsNotSelected()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).assertIsSelected()
    }
  }

  @Test
  fun givenReadyChange_thenNavigationItemsCanBeSelectedOnClick() {
    every { LocalTime.now(any<ZoneId>()) } returns LocalTime.NOON.minusHours(1L)

    with(composeTestRule) {
      setContent { ReadyChangeTestDayScreen() }

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).assertIsSelected()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).assertIsNotSelected()

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).performClick()

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).assertIsNotSelected()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).assertIsSelected()

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).performClick()

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).assertIsSelected()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).assertIsNotSelected()
    }
  }
}
