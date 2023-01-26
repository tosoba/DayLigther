package com.trm.daylighter.domain.model

sealed class Loadable<out T : Any> {
  open val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingFirst

  open val copyWithClearedError: Loadable<T>
    get() = Empty

  open fun copyWithError(error: Throwable?): Loadable<T> = FailedFirst(error)

  abstract fun <R : Any> map(block: (T) -> R): Loadable<R>

  inline fun <reified E> isFailedWith(): Boolean = (this as? Failed)?.error is E
}

sealed class WithData<T : Any> : Loadable<T>() {
  abstract val data: T
}

sealed class WithoutData : Loadable<Nothing>()

object Empty : WithoutData() {
  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

interface LoadingInProgress

object LoadingFirst : WithoutData(), LoadingInProgress {
  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

data class LoadingNext<T : Any>(override val data: T) : WithData<T>(), LoadingInProgress {
  override val copyWithLoadingInProgress: Loadable<T>
    get() = this

  override val copyWithClearedError: Loadable<T>
    get() = this

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): LoadingNext<R> = LoadingNext(block(data))
}

interface Failed {
  val error: Throwable?
}

data class FailedFirst(override val error: Throwable?) : WithoutData(), Failed {
  override val copyWithLoadingInProgress: LoadingFirst
    get() = LoadingFirst

  override fun <R : Any> map(block: (Nothing) -> R): Loadable<R> = this
}

data class FailedNext<T : Any>(
  override val data: T,
  override val error: Throwable?,
) : WithData<T>(), Failed {
  override val copyWithClearedError: Ready<T>
    get() = Ready(data)

  override val copyWithLoadingInProgress: Loadable<T>
    get() = LoadingNext(data)

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): FailedNext<R> = FailedNext(block(data), error)
}

data class Ready<T : Any>(override val data: T) : WithData<T>() {
  override val copyWithLoadingInProgress: LoadingNext<T>
    get() = LoadingNext(data)

  override val copyWithClearedError: Loadable<T>
    get() = this

  override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(data, error)

  override fun <R : Any> map(block: (T) -> R): WithData<R> = Ready(block(data))
}

inline fun <reified T : Any> T?.asLoadable(): Loadable<T> = if (this == null) Empty else Ready(this)
