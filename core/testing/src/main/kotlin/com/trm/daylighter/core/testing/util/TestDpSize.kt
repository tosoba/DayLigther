package com.trm.daylighter.core.testing.util

import androidx.compose.ui.unit.DpSize

fun testDpSize(width: TestWidthClass, height: TestHeightClass): DpSize =
  DpSize(width = width.size, height = height.size)

fun testScreenDpSizeCombinations(): List<Array<Any>> =
  TestWidthClass.entries.flatMap { width ->
    TestHeightClass.entries.map { height -> arrayOf(width, height) }
  }
