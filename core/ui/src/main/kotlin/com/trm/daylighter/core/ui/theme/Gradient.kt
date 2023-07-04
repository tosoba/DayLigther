package com.trm.daylighter.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val backgroundToTransparentVerticalGradient: Brush
  @Composable
  get() =
    Brush.verticalGradient(0f to MaterialTheme.colorScheme.background, 1f to Color.Transparent)

val surfaceToTransparentVerticalGradient: Brush
  @Composable
  get() =
    Brush.verticalGradient(0f to MaterialTheme.colorScheme.surface, 1f to Color.Transparent)

