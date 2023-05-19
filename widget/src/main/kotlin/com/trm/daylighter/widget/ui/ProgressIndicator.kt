package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.layout.Alignment

@Composable
internal fun ProgressIndicator() {
  AppWidgetBox(contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = GlanceTheme.colors.onPrimary)
  }
}
