package com.trm.daylighter.core.ui.util.ext

import androidx.compose.ui.graphics.Color
import com.trm.daylighter.core.common.model.DayPeriod
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.blueHourColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.goldenHourColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor

fun DayPeriod.color(): Color =
  when (this) {
    DayPeriod.NIGHT -> nightColor
    DayPeriod.ASTRONOMICAL -> astronomicalTwilightColor
    DayPeriod.NAUTICAL -> nauticalTwilightColor
    DayPeriod.CIVIL -> civilTwilightColor
    DayPeriod.DAY -> dayColor
    DayPeriod.GOLDEN_HOUR -> goldenHourColor
    DayPeriod.BLUE_HOUR -> blueHourColor
  }

fun DayPeriod.textColor(): Color = if (this == DayPeriod.DAY) Color.Black else Color.White

fun DayPeriod.textShadowColor(): Color = if (this == DayPeriod.DAY) Color.White else Color.Black
