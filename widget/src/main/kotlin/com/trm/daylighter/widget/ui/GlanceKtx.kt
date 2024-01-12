package com.trm.daylighter.widget.ui

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.trm.daylighter.core.common.di.provider.ClassProvider

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
internal fun stringResource(@StringRes id: Int, args: List<Any> = emptyList()): String =
  LocalContext.current.getString(id, *args.toTypedArray())

internal val Float.toPx: Float
  get() = this * Resources.getSystem().displayMetrics.density

internal val Float.toDp: Float
  get() = this / Resources.getSystem().displayMetrics.density

@Composable
internal fun deepLinkAction(uri: Uri): Action =
  actionStartActivity(
    Intent(Intent.ACTION_VIEW, uri, LocalContext.current, LocalClassProvider.current.clazz)
  )

internal val LocalClassProvider =
  staticCompositionLocalOf<ClassProvider> {
    throw IllegalStateException("No default class provider.")
  }
