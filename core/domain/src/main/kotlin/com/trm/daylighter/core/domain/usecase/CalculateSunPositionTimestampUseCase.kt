package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.HalfDay
import com.trm.daylighter.core.domain.model.SunPosition
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.*

class CalculateSunPositionTimestampUseCase @Inject constructor() {
  operator fun invoke(
    latitude: Double,
    longitude: Double,
    date: LocalDateTime,
    sunPosition: SunPosition,
    halfDay: HalfDay,
    timeZone: TimeZone
  ): LocalDateTime? {
    require(latitude in -90.0..90.0)
    require(longitude in -180.0..180.0)

    // first calculate the day of the year
    val day = date.atZone(ZoneOffset.UTC).dayOfYear

    // longitude to hour value and calculate an approx. time
    val lngHour = longitude / 15
    val hourTime = if (halfDay == HalfDay.MORNING) 6.0 else 18.0
    val t = day + (hourTime - lngHour) / 24

    // Calculate the suns mean anomaly
    val m = (0.9856 * t) - 3.289

    // Calculate the sun's true longitude
    val subexpression1 = 1.916 * sin(m.radians)
    val subexpression2 = 0.020 * sin(2 * m.radians)
    var l = m + subexpression1 + subexpression2 + 282.634
    l = l.normalise(360.0)

    // sun's right ascension
    var ra = atan(0.91764 * tan(l.radians)).degrees
    ra = ra.normalise(360.0)

    // RA value needs to be in the same quadrant as L
    val lQuadrant = floor(l / 90) * 90
    val raQuadrant = floor(ra / 90) * 90
    ra += (lQuadrant - raQuadrant)
    // RA into hours
    ra /= 15

    // declination
    val sinDec = 0.39782 * sin(l.radians)
    val cosDec = cos(asin(sinDec))

    // calculate zenith (point right above viewer)
    val zenith = -1 * sunPosition.degrees + 90

    // local hour angle
    val cosH =
      (cos(zenith.radians) - (sinDec * sin(latitude.radians))) / (cosDec * cos(latitude.radians))

    // no transition
    if (cosH > 1 || cosH < -1) return null

    val tempH = if (halfDay == HalfDay.MORNING) 360 - acos(cosH).degrees else acos(cosH).degrees
    val h = tempH / 15.0

    // local mean time of rising
    val ut = (h + ra - (0.06571 * t) - 6.622 - lngHour).normalise(24.0)

    val hour = floor(ut).toInt()
    val minute = floor((ut - hour) * 60.0).toInt()
    val second = ((((ut - hour) * 60) - minute) * 60.0).toInt()
    val shouldBeYesterday = lngHour > 0 && ut > 12 && halfDay == HalfDay.MORNING
    val shouldBeTomorrow = lngHour < 0 && ut < 12 && halfDay == HalfDay.EVENING
    val setDate =
      when {
        shouldBeYesterday -> date.minusDays(1)
        shouldBeTomorrow -> date.plusDays(1)
        else -> date
      }
    val timezoneOffset =
      TimeUnit.HOURS.convert(
        timeZone.getOffset(date.atZone(ZoneId.of(timeZone.id)).toInstant().toEpochMilli()).toLong(),
        TimeUnit.MILLISECONDS
      )
    return setDate.withHour(hour).withMinute(minute).withSecond(second).plusHours(timezoneOffset)
  }

  /** Convert from degrees to radians */
  private val Double.radians: Double
    get() = this * PI / 180

  /** Convert from radians to degrees */
  private val Double.degrees: Double
    get() = this * 180 / PI

  /**
   * If [this] is negative, add [maximum] to [this] until [this] will be positive if [this] >
   * [maximum], subtract [maximum] from [this] until [this] will be less than [maximum]
   */
  private fun Double.normalise(maximum: Double): Double {
    var value = this
    while (value < 0) value += maximum
    while (value > maximum) value -= maximum
    return value
  }
}
