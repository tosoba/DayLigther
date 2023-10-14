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
import com.trm.daylighter.core.ui.util.permanentNavigationDrawerMinWidth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DrawerMenuIconButtonDoesNotExistTests(private val height: TestHeightClass) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun whenUsingPermanentNavigationDrawer_drawerMenuIconButtonDoesNotExist() {
    with(composeTestRule) {
      setContentHarness(DpSize(width = permanentNavigationDrawerMinWidth, height = height.size)) {
        TestDayScreen(modifier = Modifier.fillMaxSize())
      }
      onNodeWithEnumTestTag(DayTestTags.DRAWER_MENU_ICON_BUTTON).assertDoesNotExist()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<TestHeightClass> = TestHeightClass.entries.toList()
  }
}

@RunWith(Parameterized::class)
class DrawerMenuIconButtonIsDisplayedTests(
  private val width: TestWidthClass,
  private val height: TestHeightClass
) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun whenNotUsingPermanentNavigationDrawer_drawerMenuIconButtonIsDisplayed() {
    with(composeTestRule) {
      setContentHarness(DpSize(width = width.size, height = height.size)) {
        TestDayScreen(modifier = Modifier.fillMaxSize())
      }
      onNodeWithEnumTestTag(DayTestTags.DRAWER_MENU_ICON_BUTTON).assertIsDisplayed()
    }
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters
    fun parameters(): List<Array<Any>> =
      TestWidthClass.entries
        .filter { it.size < permanentNavigationDrawerMinWidth }
        .flatMap { width -> TestHeightClass.entries.map { height -> arrayOf(width, height) } }
  }
}
