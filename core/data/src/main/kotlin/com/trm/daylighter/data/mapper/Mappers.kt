package com.trm.daylighter.data.mapper

import com.trm.daylighter.core.network.model.SunriseSunsetResult
import com.trm.daylighter.database.entity.LocationEntity
import com.trm.daylighter.database.entity.SunriseSunsetEntity
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.model.SunriseSunset
import java.time.LocalDate

fun SunriseSunsetResult.asEntity(locationId: Long, date: LocalDate): SunriseSunsetEntity =
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
    date = date,
    locationId = locationId,
  )

fun SunriseSunsetEntity.asDomainModel(): SunriseSunset =
  SunriseSunset(
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

fun LocationEntity.asDomainModel(): Location =
  Location(
    id = id,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault,
    updatedAt = updatedAt
  )
