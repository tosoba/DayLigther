package com.trm.daylighter.feature.day

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpSize
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.core.testing.util.setContentHarness
import com.trm.daylighter.core.testing.util.testScreenDpSizeCombinations
import com.trm.daylighter.feature.day.composable.ReadyChangeTestDayScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DayScreenNavigationItemsEnabledTests(
  private val width: TestWidthClass,
  private val height: TestHeightClass
) {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun givenReadyChange_thenNavigationBarItemsAreEnabled() {
    with(composeTestRule) {
      setContentHarness(DpSize(width = width.size, height = height.size)) {
        ReadyChangeTestDayScreen()
      }

      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNRISE_ITEM).assertIsEnabled()
      onNodeWithEnumTestTag(DayTestTags.NAVIGATION_SUNSET_ITEM).assertIsEnabled()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Array<Any>> = testScreenDpSizeCombinations()
  }
}
