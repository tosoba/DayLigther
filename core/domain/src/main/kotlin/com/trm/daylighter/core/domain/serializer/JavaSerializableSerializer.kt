package com.trm.daylighter.core.domain.serializer

import java.io.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.apache.commons.codec.binary.Base64

class JavaSerializableSerializer<T : Serializable> : KSerializer<T> {
  private val base64 = Base64()

  override val descriptor = PrimitiveSerialDescriptor("Serializable", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): T = fromString(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: T) {
    encoder.encodeString(toString(value))
  }

  @Suppress("UNCHECKED_CAST")
  @Throws(IOException::class, ClassNotFoundException::class, ClassCastException::class)
  private fun fromString(string: String): T =
    ObjectInputStream(ByteArrayInputStream(base64.decode(string)))
      .use(ObjectInputStream::readObject) as T

  @Throws(IOException::class)
  private fun toString(serializable: T): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    return ObjectOutputStream(byteArrayOutputStream).use {
      it.writeObject(serializable)
      it.close()
      base64.encodeToString(byteArrayOutputStream.toByteArray())
    }
  }
}
