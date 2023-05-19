package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
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
