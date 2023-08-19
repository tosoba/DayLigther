package com.trm.daylighter.core.testing.util

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithTag

fun <E : Enum<E>> SemanticsNodeInteractionsProvider.onNodeWithEnumTestTag(
  enumTestTag: E,
  useUnmergedTree: Boolean = false
) = onNodeWithTag(enumTestTag.name, useUnmergedTree)
