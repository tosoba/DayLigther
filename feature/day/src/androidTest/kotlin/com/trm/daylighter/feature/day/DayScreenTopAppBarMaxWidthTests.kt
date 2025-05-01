package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.testing.model.testLocation
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.core.testing.util.setContentHarness
import com.trm.daylighter.core.testing.util.testDpSize
import com.trm.daylighter.core.testing.util.testScreenDpSizeCombinations
import com.trm.daylighter.feature.day.composable.TestDayScreen
import com.trm.daylighter.feature.day.composable.isUsingNavigationBar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DayScreenTopAppBarMaxWidthTests(
  private val width: TestWidthClass,
  private val height: TestHeightClass,
) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun givenScreenSizeUsingNavigationBar_thenUsingMaxWidthTopAppBar() {
    with(composeTestRule) {
      val size = testDpSize(width = width, height = height)
      setContentHarness(size) {
        TestDayScreen(modifier = Modifier.fillMaxSize(), locations = Ready(listOf(testLocation())))
      }
      onNodeWithEnumTestTag(DayTestTags.TOP_APP_BAR).assertWidthIsEqualTo(size.width)
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Array<Any>> =
      testScreenDpSizeCombinations().filter(::isUsingNavigationBar)
  }
}
