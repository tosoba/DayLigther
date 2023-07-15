package com.trm.daylighter.core.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.model.MapDefaults

@Composable
fun ZoomControlsRow(
  zoom: Double,
  incrementZoom: () -> Unit,
  decrementZoom: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(modifier = modifier) {
    ZoomInButton(mapZoom = zoom, onClick = { if (zoom < MapDefaults.MAX_ZOOM) incrementZoom() })
    Spacer(modifier = Modifier.width(5.dp))
    ZoomOutButton(mapZoom = zoom, onClick = { if (zoom > MapDefaults.MIN_ZOOM) decrementZoom() })
  }
}

@Composable
fun ZoomOutButton(mapZoom: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
  AnimatedVisibility(
    visible = mapZoom > MapDefaults.MIN_ZOOM,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier
  ) {
    FloatingActionButton(onClick = onClick) {
      Icon(
        imageVector = Icons.Filled.ZoomOut,
        contentDescription = stringResource(commonR.string.zoom_out)
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
    FloatingActionButton(onClick = onClick) {
      Icon(
        imageVector = Icons.Filled.ZoomIn,
        contentDescription = stringResource(commonR.string.zoom_in)
      )
    }
  }
}
