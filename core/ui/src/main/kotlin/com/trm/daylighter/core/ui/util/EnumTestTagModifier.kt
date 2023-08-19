package com.trm.daylighter.core.ui.util

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag

@Stable
fun <E : Enum<E>> Modifier.enumTestTag(tag: E) = semantics(properties = { testTag = tag.name })
