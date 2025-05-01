package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.ui.util.NoRippleInteractionSource

@Composable
fun ZoomButtonsRow(
  zoom: Double,
  incrementZoom: () -> Unit,
  decrementZoom: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier) {
    ZoomButton(
      enabled = zoom < MapDefaults.MAX_ZOOM,
      onClick = { if (zoom < MapDefaults.MAX_ZOOM) incrementZoom() },
    ) {
      Icon(
        imageVector = Icons.Filled.ZoomIn,
        contentDescription = stringResource(commonR.string.zoom_in),
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    ZoomButton(
      enabled = zoom > MapDefaults.MIN_ZOOM,
      onClick = { if (zoom > MapDefaults.MIN_ZOOM) decrementZoom() },
    ) {
      Icon(
        imageVector = Icons.Filled.ZoomOut,
        contentDescription = stringResource(commonR.string.zoom_out),
      )
    }
  }
}

@Composable
fun ZoomButton(
  modifier: Modifier = Modifier,
  enabled: Boolean,
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
) {
  FloatingActionButton(
    modifier = modifier,
    onClick = onClick,
    containerColor =
      FloatingActionButtonDefaults.containerColor.run { if (enabled) this else copy(alpha = .95f) },
    elevation =
      FloatingActionButtonDefaults.run { if (enabled) elevation() else bottomAppBarFabElevation() },
    interactionSource = if (enabled) MutableInteractionSource() else NoRippleInteractionSource(),
  ) {
    icon()
  }
}
