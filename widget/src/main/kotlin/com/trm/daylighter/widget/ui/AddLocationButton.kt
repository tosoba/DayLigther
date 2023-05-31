package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.navigation.addLocationDeeplinkUri

@Composable
internal fun AddLocationButton() {
  val context = LocalContext.current
  AppWidgetBox(contentAlignment = Alignment.Center) {
    Button(
      text = stringResource(id = R.string.add_location),
      onClick = deepLinkAction(context.addLocationDeeplinkUri())
    )
  }
}
