package com.trm.daylighter.core.domain.model

import java.io.*
import java.io.Serializable as JavaSerializable
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.apache.commons.codec.binary.Base64 as ApacheBase64

@Serializable(with = LoadableSerializer::class)
sealed interface Loadable<out T : Any> {
  val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingFirst

  val copyWithClearedError: Loadable<T>
    get() = Empty

  fun copyWithError(error: Throwable?): Loadable<T> = FailedFirst(error)

  fun <R : Any> map(block: (T) -> R): Loadable<R>
}

inline fun <reified E> Loadable<*>.isFailedWith(): Boolean = (this as? Failed)?.throwable is E

sealed interface WithData<T : Any> : Loadable<T> {
  val data: T
}

sealed interface WithoutData : Loadable<Nothing>

object Empty : WithoutData {
  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

sealed interface Loading

object LoadingFirst : WithoutData, Loading {
  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

data class LoadingNext<T : Any>(override val data: T) : WithData<T>, Loading {
  override val copyWithLoadingInProgress: Loadable<T>
    get() = this

  override val copyWithClearedError: Loadable<T>
    get() = this

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): LoadingNext<R> = LoadingNext(block(data))
}

sealed interface Failed {
  val throwable: Throwable?
}

data class FailedFirst(override val throwable: Throwable?) : WithoutData, Failed {
  override val copyWithLoadingInProgress: LoadingFirst
    get() = LoadingFirst

  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

data class FailedNext<T : Any>(
  override val data: T,
  override val throwable: Throwable?,
) : WithData<T>, Failed {
  override val copyWithClearedError: Ready<T>
    get() = Ready(data)

  override val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingNext(data)

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): FailedNext<R> = FailedNext(block(data), throwable)
}

data class Ready<T : Any>(override val data: T) : WithData<T> {
  override val copyWithLoadingInProgress: LoadingNext<T>
    get() = LoadingNext(data)

  override val copyWithClearedError: Loadable<T>
    get() = this

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): WithData<R> = Ready(block(data))
}

inline fun <reified T : Any> T?.asLoadable(): Loadable<T> = if (this == null) Empty else Ready(this)

class LoadableSerializer<T : Any>(valueSerializer: KSerializer<T>) : KSerializer<Loadable<T>> {
  @OptIn(ExperimentalSerializationApi::class)
  @Serializable
  @SerialName("ServiceResult")
  data class LoadableSurrogate<T : Any>(
    val className: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val data: T? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = JavaSerializableSerializer::class)
    val error: JavaSerializable? = null
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
          error = if (value is Failed) value.throwable else null
        )
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

class JavaSerializableSerializer<T : JavaSerializable> : KSerializer<T> {
  private val base64 = ApacheBase64()

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
