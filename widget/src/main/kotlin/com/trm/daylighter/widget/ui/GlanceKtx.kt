package com.trm.daylighter.widget.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*

@Composable
fun AppWidgetBox(
  modifier: GlanceModifier = GlanceModifier,
  contentAlignment: Alignment = Alignment.TopStart,
  content: @Composable () -> Unit
) {
  Box(
    modifier = appWidgetBackgroundModifier().then(modifier),
    contentAlignment = contentAlignment,
    content = content
  )
}

@Composable
fun AppWidgetColumn(
  modifier: GlanceModifier = GlanceModifier,
  verticalAlignment: Alignment.Vertical = Alignment.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = appWidgetBackgroundModifier().then(modifier),
    verticalAlignment = verticalAlignment,
    horizontalAlignment = horizontalAlignment,
    content = content,
  )
}

@Composable
fun AppWidgetRow(
  modifier: GlanceModifier = GlanceModifier,
  verticalAlignment: Alignment.Vertical = Alignment.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable RowScope.() -> Unit
) {
  Row(
    modifier = appWidgetBackgroundModifier().then(modifier),
    verticalAlignment = verticalAlignment,
    horizontalAlignment = horizontalAlignment,
    content = content,
  )
}

@Composable
fun appWidgetBackgroundModifier() =
  GlanceModifier.fillMaxSize()
    .padding(16.dp)
    .appWidgetBackground()
    .background(GlanceTheme.colors.primaryContainer)
    .appWidgetBackgroundCornerRadius()

fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
  if (Build.VERSION.SDK_INT >= 31) {
    cornerRadius(android.R.dimen.system_app_widget_background_radius)
  } else {
    cornerRadius(16.dp)
  }
  return this
}

fun GlanceModifier.appWidgetInnerCornerRadius(): GlanceModifier {
  if (Build.VERSION.SDK_INT >= 31) {
    cornerRadius(android.R.dimen.system_app_widget_inner_radius)
  } else {
    cornerRadius(8.dp)
  }
  return this
}

@Composable
fun stringResource(@StringRes id: Int, vararg args: Any): String =
  LocalContext.current.getString(id, args)

val Float.toPx
  get() = this * Resources.getSystem().displayMetrics.density

val Context.isNightMode: Boolean
  get() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
      Configuration.UI_MODE_NIGHT_YES
