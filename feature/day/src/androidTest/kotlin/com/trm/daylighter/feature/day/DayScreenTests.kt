package com.trm.daylighter.feature.day

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.trm.daylighter.uitesthiltmanifest.HiltComponentActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DayScreenTests {
  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

  @Before fun setup() = hiltRule.inject()

  @Test
  fun t() {
    composeTestRule.setContent {}
  }
}
