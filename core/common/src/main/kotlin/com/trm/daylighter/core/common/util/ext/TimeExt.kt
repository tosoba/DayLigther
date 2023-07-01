package com.trm.daylighter.core.common.util.ext

import android.content.Context
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.model.DayMode
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun LocalDateTime.isEqualOrAfterOtherNotNull(other: LocalDateTime?): Boolean =
  other != null && (isEqual(other) || isAfter(other))

fun LocalDateTime.isBeforeOtherNotNull(other: LocalDateTime?): Boolean =
  other != null && isBefore(other)

fun LocalDateTime.isBeforeOtherOrOtherIsNull(other: LocalDateTime?): Boolean =
  other == null || isBefore(other)

val LocalDateTime.isoLocalTimeLabel24H: String
  get() = format(DateTimeFormatter.ISO_TIME)

val LocalDateTime.isoLocalTimeLabel12H: String
  get() = format(DateTimeFormatter.ofPattern("hh:mm:ss a"))

fun LocalDateTime.timeLabel(using24HFormat: Boolean): String =
  if (using24HFormat) isoLocalTimeLabel24H else isoLocalTimeLabel12H

fun timeDifferenceLabel(from: LocalTime, to: LocalTime): String {
  val fromSecondOfDay = from.toSecondOfDay()
  val toSecondOfDay = to.toSecondOfDay()
  val diffLength = LocalTime.ofSecondOfDay(abs(toSecondOfDay - fromSecondOfDay).toLong())
  val diffPrefix =
    when {
      toSecondOfDay > fromSecondOfDay -> "+"
      toSecondOfDay < fromSecondOfDay -> "-"
      else -> ""
    }
  return formatTimeDifference(diffPrefix, diffLength)
}

fun formatTimeDifference(prefix: String, diff: LocalTime): String =
  "$prefix${diff.format(DateTimeFormatter.ISO_LOCAL_TIME).run { if (startsWith("00:")) substring(3) else this }}"

fun dayLengthDiffTime(dayLengthSeconds1: Long, dayLengthSeconds2: Long): LocalTime =
  LocalTime.ofSecondOfDay(abs(dayLengthSeconds1 - dayLengthSeconds2))

fun dayLengthDiffPrefix(todayLengthSeconds: Long, yesterdayLengthSeconds: Long): String =
  when {
    todayLengthSeconds > yesterdayLengthSeconds -> "+"
    todayLengthSeconds < yesterdayLengthSeconds -> "-"
    else -> ""
  }

fun Context.timeZoneDiffLabelBetween(from: ZonedDateTime, to: ZonedDateTime): String {
  val offsetSeconds = from.offset.totalSeconds - to.offset.totalSeconds
  if (offsetSeconds == 0) return getString(R.string.same_timezone_as_you)

  val absTimeZoneOffset = abs(offsetSeconds)
  val hours = absTimeZoneOffset / 3600
  val minutes = absTimeZoneOffset % 3600 / 60
  return if (offsetSeconds < 0) getString(R.string.timezone_behind_of_you, hours, minutes)
  else getString(R.string.timezone_ahead_of_you, hours, minutes)
}

fun formatTimeMillis(millis: Long): String =
  String.format(
    "%02d:%02d:%02d",
    TimeUnit.MILLISECONDS.toHours(millis),
    TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
    TimeUnit.MILLISECONDS.toSeconds(millis) % 60
  )

fun ZoneId.currentDayMode(): DayMode =
  if (LocalTime.now(this).isBefore(LocalTime.NOON)) DayMode.SUNRISE else DayMode.SUNSET

fun LocalTime.formatTimeUntilNow(zoneId: ZoneId) =
  formatTimeMillis(millis = secondsUntilNow(zoneId) * 1_000L)

fun LocalTime.secondsUntilNow(zoneId: ZoneId) =
  toSecondOfDay() - LocalTime.now(zoneId).toSecondOfDay()
