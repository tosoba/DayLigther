package com.trm.daylighter.core.domain.model

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Location(
  val id: Long = 0,
  val latitude: Double,
  val longitude: Double,
  val isDefault: Boolean,
  @Serializable(with = ZonedDateTimeSerializer::class) val updatedAt: ZonedDateTime,
  @Serializable(with = ZoneIdSerializer::class) val zoneId: ZoneId,
)

class ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Serializable", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ZonedDateTime) {
    encoder.encodeString(value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
  }

  override fun deserialize(decoder: Decoder): ZonedDateTime =
    ZonedDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_ZONED_DATE_TIME)
}

class ZoneIdSerializer : KSerializer<ZoneId> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Serializable", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ZoneId) {
    encoder.encodeString(value.id)
  }

  override fun deserialize(decoder: Decoder): ZoneId = ZoneId.of(decoder.decodeString())
}
