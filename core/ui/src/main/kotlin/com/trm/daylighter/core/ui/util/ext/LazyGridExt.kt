package com.trm.daylighter.core.ui.util.ext

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope

fun LazyGridItemSpanScope.fullWidthSpan(): GridItemSpan = GridItemSpan(maxLineSpan)
