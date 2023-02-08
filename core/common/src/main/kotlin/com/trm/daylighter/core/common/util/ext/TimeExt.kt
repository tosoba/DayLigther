package com.trm.daylighter.core.common.util.ext

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val ZonedDateTime.isoLocalTimeLabel: String
  get() = toLocalTime().format(DateTimeFormatter.ISO_TIME)
