package com.trm.daylighter.domain.model

sealed interface Loadable<out T : Any> {
  val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingFirst

  val copyWithClearedError: Loadable<T>
    get() = Empty

  fun copyWithError(error: Throwable?): Loadable<T> = FailedFirst(error)

  fun <R : Any> map(block: (T) -> R): Loadable<R>
}

inline fun <reified E> Loadable<*>.isFailedWith(): Boolean = (this as? Failed)?.error is E

inline fun <reified E> Loadable<*>.ifIsFailedWith(action: (E) -> Unit) {
  ((this as? Failed)?.error as? E)?.let(action)
}

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
  val error: Throwable?
}

data class FailedFirst(override val error: Throwable?) : WithoutData, Failed {
  override val copyWithLoadingInProgress: LoadingFirst
    get() = LoadingFirst

  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

data class FailedNext<T : Any>(
  override val data: T,
  override val error: Throwable?,
) : WithData<T>, Failed {
  override val copyWithClearedError: Ready<T>
    get() = Ready(data)

  override val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingNext(data)

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): FailedNext<R> = FailedNext(block(data), error)
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
