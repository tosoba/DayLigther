package com.trm.daylighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import com.trm.daylighter.core.ui.local.LocalHeightSizeClass
import com.trm.daylighter.core.ui.local.LocalWidthSizeClass
import com.trm.daylighter.core.ui.theme.DayLighterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val windowSizeClass = calculateWindowSizeClass(this)
      CompositionLocalProvider(
        LocalWidthSizeClass provides windowSizeClass.widthSizeClass,
        LocalHeightSizeClass provides windowSizeClass.heightSizeClass
      ) {
        DayLighterTheme { DayLighterMainContent() }
      }
    }
  }
}
