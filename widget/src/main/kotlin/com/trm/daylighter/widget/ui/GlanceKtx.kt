package com.trm.daylighter.widget.ui

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*

@Composable
internal fun AppWidgetBox(
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
internal fun AppWidgetColumn(
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
internal fun AppWidgetRow(
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
internal fun appWidgetBackgroundModifier() =
  GlanceModifier.fillMaxSize()
    .padding(16.dp)
    .appWidgetBackground()
    .background(GlanceTheme.colors.primaryContainer)
    .appWidgetBackgroundCornerRadius()

internal fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
  if (Build.VERSION.SDK_INT >= 31) {
    cornerRadius(android.R.dimen.system_app_widget_background_radius)
  } else {
    cornerRadius(16.dp)
  }
  return this
}

internal fun GlanceModifier.appWidgetInnerCornerRadius(): GlanceModifier {
  if (Build.VERSION.SDK_INT >= 31) {
    cornerRadius(android.R.dimen.system_app_widget_inner_radius)
  } else {
    cornerRadius(8.dp)
  }
  return this
}

@Composable
internal fun stringResource(@StringRes id: Int, vararg args: Any): String =
  LocalContext.current.getString(id, args)

internal val Float.toPx: Float
  get() = this * Resources.getSystem().displayMetrics.density

internal val Float.toDp: Float
  get() = this / Resources.getSystem().displayMetrics.density

@Composable
internal fun deepLinkAction(@StringRes uriRes: Int): Action =
  actionStartActivity(Intent(Intent.ACTION_VIEW, stringResource(uriRes).toUri()))
