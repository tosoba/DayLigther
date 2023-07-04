package com.trm.daylighter.core.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.trm.daylighter.core.common.R

@Composable
fun DrawerMenuFloatingActionButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  SmallFloatingActionButton(onClick = onClick, modifier = modifier) {
    Icon(
      imageVector = Icons.Filled.Menu,
      contentDescription = stringResource(R.string.application_menu)
    )
  }
}
