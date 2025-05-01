package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun BoxScope.LocationNameGradientOverlay() {
  Box(
    modifier =
      Modifier.matchParentSize()
        .background(
          Brush.verticalGradient(
            .6f to Color.Transparent,
            1f to MaterialTheme.colorScheme.background,
          )
        )
  )
}

@Composable
fun LocationNameLabel(name: String, modifier: Modifier = Modifier) {
  Text(
    text = name,
    color = MaterialTheme.colorScheme.onBackground,
    style = MaterialTheme.typography.titleMedium,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    textAlign = TextAlign.Center,
    modifier = modifier,
  )
}
