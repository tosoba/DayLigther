package com.trm.daylighter

/** This is shared between :app and :benchmarks module to provide configurations type safety. */
@Suppress("unused")
enum class DaylighterBuildType(val applicationIdSuffix: String? = null) {
  DEBUG(".debug"),
  RELEASE,
  BENCHMARK(".benchmark")
}
