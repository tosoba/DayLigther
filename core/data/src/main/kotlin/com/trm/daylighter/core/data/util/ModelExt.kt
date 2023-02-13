package com.trm.daylighter.core.data.util

import com.trm.daylighter.core.network.model.SunriseSunsetResult
import java.time.ZoneId

fun SunriseSunsetResult.timezoneAdjusted(zoneId: ZoneId): SunriseSunsetResult =
  copy(
    astronomicalTwilightBegin = astronomicalTwilightBegin.withZoneSameInstant(zoneId),
    astronomicalTwilightEnd = astronomicalTwilightEnd.withZoneSameInstant(zoneId),
    civilTwilightBegin = civilTwilightBegin.withZoneSameInstant(zoneId),
    civilTwilightEnd = civilTwilightEnd.withZoneSameInstant(zoneId),
    nauticalTwilightBegin = nauticalTwilightBegin.withZoneSameInstant(zoneId),
    nauticalTwilightEnd = nauticalTwilightEnd.withZoneSameInstant(zoneId),
    solarNoon = solarNoon.withZoneSameInstant(zoneId),
    sunrise = sunrise.withZoneSameInstant(zoneId),
    sunset = sunset.withZoneSameInstant(zoneId),
  )
