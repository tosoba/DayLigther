package com.trm.daylighter.core.common.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

data class LoadableParcelable<out T : Parcelable>(val loadable: Loadable<T>) : Parcelable {
  constructor(
    parcel: Parcel
  ) : this(
    when (val className = parcel.readString()) {
      null -> throw IllegalStateException()
      Empty::class.java.name -> Empty
      LoadingFirst::class.java.name -> LoadingFirst
      LoadingNext::class.java.name -> LoadingNext(parcel.readParcelableByClassName<T>(className))
      FailedFirst::class.java.name -> FailedFirst(parcel.readThrowable())
      FailedNext::class.java.name -> {
        FailedNext(
          value = parcel.readParcelableByClassName<T>(className),
          error = parcel.readThrowable(),
        )
      }
      Ready::class.java.name -> Ready(parcel.readParcelableByClassName<T>(className))
      else -> throw IllegalStateException()
    },
  )

  override fun writeToParcel(parcel: Parcel, flag: Int) {
    parcel.writeString(loadable::class.java.name)
    if (loadable is WithValue) parcel.writeParcelable(loadable.value, 0)
    if (loadable is Failed) loadable.error?.let(parcel::writeSerializable)
  }

  override fun describeContents(): Int = 0

  companion object CREATOR : Parcelable.Creator<LoadableParcelable<Parcelable>> {
    override fun createFromParcel(parcel: Parcel): LoadableParcelable<Parcelable> =
      LoadableParcelable(parcel)
    override fun newArray(size: Int): Array<LoadableParcelable<Parcelable>?> = arrayOfNulls(size)
  }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Parcelable> Parcel.readParcelableByClassName(className: String): T {
  val clazz = Class.forName(className) as? Class<T> ?: throw IllegalArgumentException()
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    readParcelable(clazz.classLoader, clazz)
  } else {
    readParcelable(clazz.classLoader)
  }
    ?: throw IllegalStateException()
}

private fun Parcel.readThrowable(): Throwable? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    readSerializable(null, Throwable::class.java)
  } else {
    readSerializable()?.let { it as? Throwable? }
  }

inline fun <reified T : Parcelable> Loadable<T>.parcelize() = LoadableParcelable(this)
