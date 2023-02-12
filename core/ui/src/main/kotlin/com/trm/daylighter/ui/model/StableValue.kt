package com.trm.daylighter.ui.model

import androidx.compose.runtime.Stable

@Stable data class StableValue<T>(val value: T)

fun <T> T.asStable(): StableValue<T> = StableValue(this)
