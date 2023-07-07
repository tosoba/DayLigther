package com.trm.daylighter.core.domain.model

enum class SunPosition {
  OFFICIAL,
  GOLDEN_HOUR_ABOVE,
  GOLDEN_HOUR_BELOW,
  CIVIL,
  NAUTICAL,
  ASTRONOMICAL;

  val degrees: Double
    get() =
      when (this) {
        OFFICIAL -> -35.0 / 60.0
        GOLDEN_HOUR_ABOVE -> 6.0
        GOLDEN_HOUR_BELOW -> -4.0
        CIVIL -> -6.0
        NAUTICAL -> -12.0
        ASTRONOMICAL -> -18.0
      }
}
