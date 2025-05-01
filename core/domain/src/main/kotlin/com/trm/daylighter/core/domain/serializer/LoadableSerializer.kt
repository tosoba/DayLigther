package com.trm.daylighter.core.domain.serializer

import com.trm.daylighter.core.domain.model.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LoadableSerializer<T : Any>(valueSerializer: KSerializer<T>) : KSerializer<Loadable<T>> {
  @OptIn(ExperimentalSerializationApi::class)
  @Serializable
  @SerialName("ServiceResult")
  data class LoadableSurrogate<T : Any>(
    val className: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val data: T? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = JavaSerializableSerializer::class)
    val error: java.io.Serializable? = null,
  )

  private val surrogateSerializer = LoadableSurrogate.serializer(valueSerializer)

  override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

  override fun serialize(encoder: Encoder, value: Loadable<T>) {
    encoder.encodeSerializableValue(
      serializer = surrogateSerializer,
      value =
        LoadableSurrogate(
          className = value::class.java.name,
          data = if (value is WithData) value.data else null,
          error = if (value is Failed) value.throwable else null,
        ),
    )
  }

  override fun deserialize(decoder: Decoder): Loadable<T> {
    val surrogate = surrogateSerializer.deserialize(decoder)
    return when (surrogate.className) {
      Empty::class.java.name -> Empty
      LoadingFirst::class.java.name -> LoadingFirst
      LoadingNext::class.java.name -> LoadingNext(requireNotNull(surrogate.data))
      FailedFirst::class.java.name -> FailedFirst(surrogate.error as? Throwable)
      FailedNext::class.java.name -> {
        FailedNext(data = requireNotNull(surrogate.data), throwable = surrogate.error as? Throwable)
      }
      Ready::class.java.name -> Ready(requireNotNull(surrogate.data))
      else -> throw IllegalStateException()
    }
  }
}
