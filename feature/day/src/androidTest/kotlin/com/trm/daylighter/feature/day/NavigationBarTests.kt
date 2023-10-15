package com.trm.daylighter.feature.day

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.FailedFirst
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.testing.model.testLocation
import com.trm.daylighter.core.testing.model.testSunriseSunset
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.core.testing.util.setContentHarness
import com.trm.daylighter.core.testing.util.testScreenDpSizeCombinations
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.feature.day.composable.TestDayScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NavigationBarIsDisplayedTests(
  private val width: TestWidthClass,
  private val height: TestHeightClass
) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun whenUsingNavigationBar_navigationBarIsDisplayed() {
    with(composeTestRule) {
      setContentHarness(DpSize(width = width.size, height = height.size)) {
        TestDayScreen(modifier = Modifier.fillMaxSize())
      }
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_BAR).assertIsDisplayed()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Array<Any>> =
      testScreenDpSizeCombinations().filter { (width, height) ->
        width == TestWidthClass.COMPACT || height == TestHeightClass.EXPANDED
      }
  }
}

@RunWith(Parameterized::class)
class NavigationBarIsDoesNotExistTests(
  private val width: TestWidthClass,
  private val height: TestHeightClass
) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun whenNotUsingNavigationBar_navigationBarIsNotDisplayed() {
    with(composeTestRule) {
      setContentHarness(DpSize(width = width.size, height = height.size)) {
        TestDayScreen(modifier = Modifier.fillMaxSize())
      }
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_BAR).assertDoesNotExist()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Array<Any>> =
      testScreenDpSizeCombinations().filter { (width, height) ->
        width != TestWidthClass.COMPACT && height != TestHeightClass.EXPANDED
      }
  }
}

@RunWith(Parameterized::class)
class NavigationBarItemsDisabledTests(
  private val changeFlow: Flow<StableLoadable<LocationSunriseSunsetChange>>
) {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun whenCurrentLocationSunriseSunsetChangeIsNotReady_navigationBarItemsAreDisabled() {
    with(composeTestRule) {
      setContentHarness(
        DpSize(width = TestWidthClass.COMPACT.size, height = TestHeightClass.EXPANDED.size)
      ) {
        TestDayScreen(
          modifier = Modifier.fillMaxSize(),
          locations = Ready(listOf(testLocation())),
          sunriseSunsetChangeInLocationAt = { changeFlow }
        )
      }

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_BAR_SUNRISE_ITEM).assertIsNotEnabled()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_BAR_SUNSET_ITEM).assertIsNotEnabled()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Any> =
      listOf(
        flowOf(Empty.asStable<LocationSunriseSunsetChange>()),
        flowOf(LoadingFirst.asStable<LocationSunriseSunsetChange>()),
        flowOf(FailedFirst(Throwable()).asStable<LocationSunriseSunsetChange>()),
      )
  }
}

class NavigationBarItemsEnabledTests {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun whenCurrentLocationSunriseSunsetChangeIsReady_navigationBarItemsAreEnabled() {
    with(composeTestRule) {
      setContentHarness(
        DpSize(width = TestWidthClass.COMPACT.size, height = TestHeightClass.EXPANDED.size)
      ) {
        TestDayScreen(
          modifier = Modifier.fillMaxSize(),
          locations = Ready(listOf(testLocation())),
          sunriseSunsetChangeInLocationAt = {
            flowOf(
              Ready(
                  LocationSunriseSunsetChange(
                    location = testLocation(),
                    today = testSunriseSunset(),
                    yesterday = testSunriseSunset()
                  )
                )
                .asStable()
            )
          }
        )
      }

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_BAR_SUNRISE_ITEM).assertIsEnabled()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_BAR_SUNSET_ITEM).assertIsEnabled()
    }
  }
}
