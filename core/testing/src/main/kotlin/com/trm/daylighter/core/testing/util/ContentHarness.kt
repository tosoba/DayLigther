package com.trm.daylighter.core.testing.util

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.unit.DpSize
import com.google.accompanist.testharness.TestHarness
import com.trm.daylighter.core.ui.local.LocalHeightSizeClass
import com.trm.daylighter.core.ui.local.LocalWidthSizeClass
import org.junit.rules.TestRule

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.setContentHarness(
  size: DpSize,
  composable: @Composable () -> Unit
) {
  setContent {
    TestHarness(size = size) {
      val sizeClass = WindowSizeClass.calculateFromSize(size)
      CompositionLocalProvider(
        LocalWidthSizeClass provides sizeClass.widthSizeClass,
        LocalHeightSizeClass provides sizeClass.heightSizeClass
      ) {
        composable()
      }
    }
  }
}
