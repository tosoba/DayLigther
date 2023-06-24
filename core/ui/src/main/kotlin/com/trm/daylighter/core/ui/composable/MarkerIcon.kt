package com.trm.daylighter.core.ui.composable

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.trm.daylighter.core.common.R

@Composable
fun MarkerIcon(modifier: Modifier = Modifier) {
  Icon(
    painter = painterResource(id = R.drawable.marker),
    contentDescription = stringResource(R.string.location_marker),
    modifier = modifier
  )
}
