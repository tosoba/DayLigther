package com.trm.daylighter.core.common.util.ext

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val ZonedDateTime.isoLocalTimeLabel24H: String
  get() = toLocalTime().format(DateTimeFormatter.ISO_TIME)

val ZonedDateTime.isoLocalTimeLabel12H: String
  get() = toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss"))

fun ZonedDateTime.timeLabel(using24HFormat: Boolean): () -> String =
  if (using24HFormat) ::isoLocalTimeLabel24H else ::isoLocalTimeLabel12H
