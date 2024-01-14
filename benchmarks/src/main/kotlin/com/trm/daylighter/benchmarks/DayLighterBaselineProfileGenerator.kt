package com.trm.daylighter.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
