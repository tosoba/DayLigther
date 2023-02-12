package com.trm.daylighter.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.MapDefaults

@Composable
fun ZoomOutButton(mapZoom: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
  AnimatedVisibility(
    visible = mapZoom > MapDefaults.MIN_ZOOM,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier
  ) {
    SmallFloatingActionButton(onClick = onClick) {
      Icon(
        imageVector = Icons.Filled.ZoomOut,
        contentDescription = stringResource(id = commonR.string.zoom_out)
      )
    }
  }
}

@Composable
fun ZoomInButton(mapZoom: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
  AnimatedVisibility(
    visible = mapZoom < MapDefaults.MAX_ZOOM,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier
  ) {
    SmallFloatingActionButton(onClick = onClick) {
      Icon(
        imageVector = Icons.Filled.ZoomIn,
        contentDescription = stringResource(id = commonR.string.zoom_in)
      )
    }
  }
}
