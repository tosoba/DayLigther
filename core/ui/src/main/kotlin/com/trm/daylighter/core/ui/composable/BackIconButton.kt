package com.trm.daylighter.core.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.trm.daylighter.core.common.R

@Composable
fun BackIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  IconButton(onClick = onClick, modifier = modifier) {
    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
  }
}
