package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.navigation.newLocationDeeplinkUri

@Composable
internal fun NewLocationButton() {
  val context = LocalContext.current
  AppWidgetBox(contentAlignment = Alignment.Center) {
    Button(
      text = stringResource(R.string.new_location),
      onClick = deepLinkAction(context.newLocationDeeplinkUri())
    )
  }
}
