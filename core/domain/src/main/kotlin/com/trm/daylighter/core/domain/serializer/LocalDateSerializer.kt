package com.trm.daylighter.core.domain.serializer

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LocalDateSerializer : KSerializer<LocalDate> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Serializable", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalDate) {
    encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
  }

  override fun deserialize(decoder: Decoder): LocalDate =
    LocalDate.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE)
}
