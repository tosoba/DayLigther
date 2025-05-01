package com.trm.daylighter

/** This is shared between :app and :benchmarks module to provide configurations type safety. */
enum class DayLighterBuildType(val applicationIdSuffix: String? = null) {
  DEBUG(".debug"),
  RELEASE,
  BENCHMARK(".benchmark"),
}
