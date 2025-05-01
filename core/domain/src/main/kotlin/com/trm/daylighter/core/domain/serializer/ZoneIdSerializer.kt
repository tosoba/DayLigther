package com.trm.daylighter.core.domain.serializer

import java.time.ZoneId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ZoneIdSerializer : KSerializer<ZoneId> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Serializable", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ZoneId) {
    encoder.encodeString(value.id)
  }

  override fun deserialize(decoder: Decoder): ZoneId = ZoneId.of(decoder.decodeString())
}
