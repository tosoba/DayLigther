package com.trm.daylighter.core.ui.model

import androidx.compose.runtime.Stable
import com.trm.daylighter.core.domain.model.Loadable

@Stable data class StableValue<T>(val value: T)

fun <T> T.asStable(): StableValue<T> = StableValue(this)

fun <T : Any> Loadable<T>.asStable(): StableValue<Loadable<T>> = StableValue(this)

typealias StableLoadable<T> = StableValue<Loadable<T>>
