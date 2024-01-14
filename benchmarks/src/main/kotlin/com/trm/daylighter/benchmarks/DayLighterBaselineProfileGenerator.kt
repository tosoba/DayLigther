package com.trm.daylighter.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

class DayLighterBaselineProfileGenerator {
  @get:Rule val benchmarkRule = BaselineProfileRule()

  @Test
  fun generate() {
    benchmarkRule.collect(packageName = "com.trm.daylighter.prod.benchmark") {
      pressHome()
      startActivityAndWait()
    }
  }
}
