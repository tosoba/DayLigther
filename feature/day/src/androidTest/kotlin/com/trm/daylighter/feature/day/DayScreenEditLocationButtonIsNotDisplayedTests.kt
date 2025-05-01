package com.trm.daylighter.feature.day

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpSize
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.FailedFirst
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.testing.model.testLocation
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
class DayScreenEditLocationButtonIsNotDisplayedTests(
  private val changeFlow: Flow<StableLoadable<LocationSunriseSunsetChange>>,
  private val width: TestWidthClass,
  private val height: TestHeightClass,
) {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun givenNonReadyChange_thenEditLocationButtonDoesNotExist() {
    with(composeTestRule) {
      setContentHarness(DpSize(width = width.size, height = height.size)) {
        TestDayScreen(
          modifier = Modifier.fillMaxSize(),
          locations = Ready(listOf(testLocation())),
          sunriseSunsetChangeInLocationAt = { changeFlow },
        )
      }

      onNodeWithEnumTestTag(DayTestTags.EDIT_LOCATION_BUTTON).assertDoesNotExist()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Array<Any>> {
      val notReadyChangeFlows =
        listOf(
          flowOf(Empty.asStable<LocationSunriseSunsetChange>()),
          flowOf(LoadingFirst.asStable<LocationSunriseSunsetChange>()),
          flowOf(FailedFirst(Throwable()).asStable<LocationSunriseSunsetChange>()),
        )
      return testScreenDpSizeCombinations().flatMap { (width, height) ->
        notReadyChangeFlows.map { change -> arrayOf(change, width, height) }
      }
    }
  }
}
