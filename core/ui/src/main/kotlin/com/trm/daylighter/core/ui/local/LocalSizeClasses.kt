package com.trm.daylighter.core.ui.local

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }
val LocalHeightSizeClass = compositionLocalOf { WindowHeightSizeClass.Compact }

val usingPermanentNavigationDrawer: Boolean
  @Composable
  get() =
    LocalWidthSizeClass.current == WindowWidthSizeClass.Expanded &&
      LocalHeightSizeClass.current != WindowHeightSizeClass.Compact
