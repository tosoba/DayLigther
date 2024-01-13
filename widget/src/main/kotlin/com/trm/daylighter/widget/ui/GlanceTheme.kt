package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.glance.LocalSize
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import com.trm.daylighter.core.ui.theme.DarkColorScheme
import com.trm.daylighter.core.ui.theme.LightColorScheme

internal object GlanceTheme {
  val colors: ColorProviders
    @Composable @ReadOnlyComposable get() = LocalColorProviders.current
}

internal val LocalColorProviders = staticCompositionLocalOf { dynamicThemeColorProviders() }

@Composable
internal fun GlanceTheme(
  colors: ColorProviders = GlanceTheme.colors,
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(LocalColorProviders provides colors) { content() }
}

internal fun dynamicThemeColorProviders(): ColorProviders =
  ColorProviders(light = LightColorScheme, dark = DarkColorScheme)

internal val smallFontSize: Float
  @Composable
  get() =
    with(LocalSize.current) {
      when {
        width > 200.dp && height > 250.dp -> 16f
        width > 175.dp && height > 200.dp -> 14f
        else -> 12f
      }
    }

internal val mediumFontSize: Float
  @Composable
  get() =
    with(LocalSize.current) {
      when {
        width > 200.dp && height > 250.dp -> 18f
        width > 175.dp && height > 200.dp -> 16f
        else -> 14f
      }
    }

internal val largeFontSize: Float
  @Composable
  get() =
    with(LocalSize.current) {
      when {
        width > 200.dp && height > 250.dp -> 30f
        width > 175.dp && height > 200.dp -> 26f
        else -> 22f
      }
    }
