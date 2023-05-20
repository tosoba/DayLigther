package com.trm.daylighter.core.domain.model

enum class DawnOrTwilight {
  ASTRONOMICAL,
  NAUTICAL,
  CIVIL,
  OFFICIAL;

  val degrees: Double
    get() =
      when (this) {
        OFFICIAL -> -35.0 / 60.0
        CIVIL -> -6.0
        NAUTICAL -> -12.0
        ASTRONOMICAL -> -18.0
      }
}
