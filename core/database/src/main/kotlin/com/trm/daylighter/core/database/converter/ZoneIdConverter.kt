package com.trm.daylighter.core.database.converter

import androidx.room.TypeConverter
import java.time.ZoneId

object ZoneIdConverter {
  @TypeConverter fun fromStringId(value: String): ZoneId = ZoneId.of(value)
  @TypeConverter fun stringId(value: ZoneId): String = value.id
}
