package com.trm.daylighter.core.network.serializer

import java.time.ZonedDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ZonedDateTime) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): ZonedDateTime =
    ZonedDateTime.parse(decoder.decodeString())
}
