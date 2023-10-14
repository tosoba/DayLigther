package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import com.trm.daylighter.core.testing.util.TestHeightClass
import com.trm.daylighter.core.testing.util.TestWidthClass
import com.trm.daylighter.core.testing.util.onNodeWithEnumTestTag
import com.trm.daylighter.core.testing.util.setContentHarness
import com.trm.daylighter.feature.day.composable.TestDayScreen
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
    fun parameters(): List<Array<out Any>> =
      TestHeightClass.entries
        .map<TestHeightClass, Array<Any>> { height -> arrayOf(TestWidthClass.COMPACT, height) }
        .plus(TestWidthClass.entries.map { width -> arrayOf(width, TestHeightClass.EXPANDED) })
        .distinct()
  }
}
