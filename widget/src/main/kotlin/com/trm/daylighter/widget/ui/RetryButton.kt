package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.action.Action
import androidx.glance.layout.Alignment
import com.trm.daylighter.core.common.R

@Composable
internal fun RetryButton(onClick: Action) {
  AppWidgetBox(contentAlignment = Alignment.Center) {
    Button(text = stringResource(id = R.string.retry), onClick = onClick)
  }
}
