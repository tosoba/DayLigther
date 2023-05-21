package com.trm.daylighter.core.network.serializer

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateTimeSerializer : KSerializer<LocalDateTime?> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

  @OptIn(ExperimentalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: LocalDateTime?) {
    value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)?.let(encoder::encodeString)
      ?: run { encoder.encodeNull() }
  }

  override fun deserialize(decoder: Decoder): LocalDateTime? =
    ZonedDateTime.parse(decoder.decodeString()).run {
      if (year < 2000) null else LocalDateTime.ofInstant(toInstant(), this.zone)
    }
}
