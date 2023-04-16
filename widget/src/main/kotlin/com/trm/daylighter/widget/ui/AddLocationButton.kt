package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.layout.Alignment
import com.trm.daylighter.core.common.R

@Composable
internal fun AddLocationButton() {
    AppWidgetBox(contentAlignment = Alignment.Center) {
        Button(
            text = stringResource(id = R.string.add_location),
            onClick = deepLinkAction(uriRes = R.string.add_location_deep_link_uri)
        )
    }
}