package com.trm.daylighter.core.testing.util

import androidx.compose.ui.unit.DpSize

fun testDpSize(width: TestWidthClass, height: TestHeightClass): DpSize =
  DpSize(width = width.size, height = height.size)
