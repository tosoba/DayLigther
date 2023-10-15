package com.trm.daylighter.core.testing.model

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.SunriseSunset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun testLocation(
  id: Long = 0,
  latitude: Double = 0.0,
  longitude: Double = 0.0,
  name: String = "",
  isDefault: Boolean = true,
  updatedAt: LocalDateTime = LocalDateTime.now(),
  zoneId: ZoneId = ZoneId.systemDefault()
): Location =
  Location(
    id = id,
    latitude = latitude,
    longitude = longitude,
    name = name,
    isDefault = isDefault,
    updatedAt = updatedAt,
    zoneId = zoneId
  )

fun testSunriseSunset(
  morning18Below: LocalDateTime? = null,
  evening18Below: LocalDateTime? = null,
  morning6Below: LocalDateTime? = null,
  evening6Below: LocalDateTime? = null,
  morning12Below: LocalDateTime? = null,
  evening12Below: LocalDateTime? = null,
  sunrise: LocalDateTime? = null,
  sunset: LocalDateTime? = null,
  morning6Above: LocalDateTime? = null,
  morning4Below: LocalDateTime? = null,
  evening6Above: LocalDateTime? = null,
  evening4Below: LocalDateTime? = null,
  date: LocalDate = LocalDate.now(),
): SunriseSunset =
  SunriseSunset(
    morning18Below = morning18Below,
    evening18Below = evening18Below,
    morning6Below = morning6Below,
    evening6Below = evening6Below,
    morning12Below = morning12Below,
    evening12Below = evening12Below,
    sunrise = sunrise,
    sunset = sunset,
    morning6Above = morning6Above,
    morning4Below = morning4Below,
    evening6Above = evening6Above,
    evening4Below = evening4Below,
    date = date
  )
