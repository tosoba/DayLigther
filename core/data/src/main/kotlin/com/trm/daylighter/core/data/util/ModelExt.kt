package com.trm.daylighter.core.data.util

import com.trm.daylighter.core.network.model.SunriseSunsetResult
import java.time.ZoneId

fun SunriseSunsetResult.timezoneAdjusted(zoneId: ZoneId): SunriseSunsetResult =
  copy(
    astronomicalTwilightBegin = astronomicalTwilightBegin?.atZone(zoneId)?.toLocalDateTime(),
    astronomicalTwilightEnd = astronomicalTwilightEnd?.atZone(zoneId)?.toLocalDateTime(),
    civilTwilightBegin = civilTwilightBegin?.atZone(zoneId)?.toLocalDateTime(),
    civilTwilightEnd = civilTwilightEnd?.atZone(zoneId)?.toLocalDateTime(),
    nauticalTwilightBegin = nauticalTwilightBegin?.atZone(zoneId)?.toLocalDateTime(),
    nauticalTwilightEnd = nauticalTwilightEnd?.atZone(zoneId)?.toLocalDateTime(),
    solarNoon = solarNoon?.atZone(zoneId)?.toLocalDateTime(),
    sunrise = sunrise?.atZone(zoneId)?.toLocalDateTime(),
    sunset = sunset?.atZone(zoneId)?.toLocalDateTime(),
  )
