package com.trm.daylighter.core.ui.util

import androidx.compose.material3.DrawerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

val usingPermanentNavigationDrawer: Boolean
  @Composable
  get() = LocalConfiguration.current.screenWidthDp.dp >= DrawerDefaults.MaximumDrawerWidth * 3
