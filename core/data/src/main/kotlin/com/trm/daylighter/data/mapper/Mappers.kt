package com.trm.daylighter.data.mapper

import com.trm.daylighter.core.network.model.SunriseSunsetResult
import com.trm.daylighter.database.entity.SunriseSunsetEntity
import java.time.LocalDate

fun SunriseSunsetResult.asEntity(date: LocalDate): SunriseSunsetEntity =
  SunriseSunsetEntity(
    astronomicalTwilightBegin = astronomicalTwilightBegin,
    astronomicalTwilightEnd = astronomicalTwilightEnd,
    civilTwilightBegin = civilTwilightBegin,
    civilTwilightEnd = civilTwilightEnd,
    dayLengthSeconds = dayLengthSeconds,
    nauticalTwilightBegin = nauticalTwilightBegin,
    nauticalTwilightEnd = nauticalTwilightEnd,
    solarNoon = solarNoon,
    sunrise = sunrise,
    sunset = sunset,
    date = date
  )
