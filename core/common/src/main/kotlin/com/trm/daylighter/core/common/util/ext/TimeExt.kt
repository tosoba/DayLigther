package com.trm.daylighter.core.common.util.ext

import android.content.Context
import com.trm.daylighter.core.common.R
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

fun LocalDateTime.isEqualOrAfterOtherNotNull(other: LocalDateTime?): Boolean =
  other != null && (isEqual(other) || isAfter(other))

fun LocalDateTime.isBeforeOtherNotNull(other: LocalDateTime?): Boolean =
  other != null && isBefore(other)

val LocalDateTime.isoLocalTimeLabel24H: String
  get() = format(DateTimeFormatter.ISO_TIME)

val LocalDateTime.isoLocalTimeLabel12H: String
  get() = format(DateTimeFormatter.ofPattern("hh:mm:ss a"))

fun LocalDateTime.timeLabel(using24HFormat: Boolean): () -> String =
  if (using24HFormat) ::isoLocalTimeLabel24H else ::isoLocalTimeLabel12H

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

fun dayLengthDiffTime(dayLengthSeconds1: Int, dayLengthSeconds2: Int): LocalTime =
  LocalTime.ofSecondOfDay(abs(dayLengthSeconds1 - dayLengthSeconds2).toLong())

fun dayLengthDiffPrefix(todayLengthSeconds: Int, yesterdayLengthSeconds: Int): String =
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
