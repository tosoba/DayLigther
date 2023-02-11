package com.trm.daylighter.core.common.util.ext

import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

val ZonedDateTime.isoLocalTimeLabel24H: String
  get() = format(DateTimeFormatter.ISO_TIME)

val ZonedDateTime.isoLocalTimeLabel12H: String
  get() = format(DateTimeFormatter.ofPattern("hh:mm:ss"))

fun ZonedDateTime.timeLabel(using24HFormat: Boolean): () -> String =
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
