package com.trm.daylighter.core.domain.model

enum class SunPosition {
  OFFICIAL,
  GOLDEN_HOUR,
  BLUE_HOUR,
  CIVIL,
  NAUTICAL,
  ASTRONOMICAL;

  val degrees: Double
    get() =
      when (this) {
        OFFICIAL -> -35.0 / 60.0
        GOLDEN_HOUR -> -4.0
        CIVIL,
        BLUE_HOUR -> -6.0
        NAUTICAL -> -12.0
        ASTRONOMICAL -> -18.0
      }
}
