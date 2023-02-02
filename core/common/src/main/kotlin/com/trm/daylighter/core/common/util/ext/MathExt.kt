package com.trm.daylighter.core.common.util.ext

import kotlin.math.PI

val Float.radians: Float
  get() = this * PI.toFloat() / 180f
