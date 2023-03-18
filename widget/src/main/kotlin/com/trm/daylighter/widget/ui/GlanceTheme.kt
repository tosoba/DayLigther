package com.trm.daylighter.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.glance.appwidget.unit.ColorProvider
import androidx.glance.unit.ColorProvider
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

internal data class ColorProviders(
  val primary: ColorProvider,
  val onPrimary: ColorProvider,
  val primaryContainer: ColorProvider,
  val onPrimaryContainer: ColorProvider,
  val secondary: ColorProvider,
  val onSecondary: ColorProvider,
  val secondaryContainer: ColorProvider,
  val onSecondaryContainer: ColorProvider,
  val tertiary: ColorProvider,
  val onTertiary: ColorProvider,
  val tertiaryContainer: ColorProvider,
  val onTertiaryContainer: ColorProvider,
  val error: ColorProvider,
  val errorContainer: ColorProvider,
  val onError: ColorProvider,
  val onErrorContainer: ColorProvider,
  val background: ColorProvider,
  val onBackground: ColorProvider,
  val surface: ColorProvider,
  val onSurface: ColorProvider,
  val surfaceVariant: ColorProvider,
  val onSurfaceVariant: ColorProvider,
  val outline: ColorProvider,
  val textColorPrimary: ColorProvider,
  val textColorSecondary: ColorProvider,
  val inverseOnSurface: ColorProvider,
  val inverseSurface: ColorProvider,
  val inversePrimary: ColorProvider,
  val inverseTextColorPrimary: ColorProvider,
  val inverseTextColorSecondary: ColorProvider,
)

internal fun dynamicThemeColorProviders(): ColorProviders =
  ColorProviders(
    primary =
      ColorProvider(
        day = LightColorScheme.primary,
        night = DarkColorScheme.primary,
      ),
    onPrimary =
      ColorProvider(
        day = LightColorScheme.onPrimary,
        night = DarkColorScheme.onPrimary,
      ),
    primaryContainer =
      ColorProvider(
        day = LightColorScheme.primaryContainer,
        night = DarkColorScheme.primaryContainer,
      ),
    onPrimaryContainer =
      ColorProvider(
        day = LightColorScheme.onPrimaryContainer,
        night = DarkColorScheme.onPrimaryContainer,
      ),
    secondary =
      ColorProvider(
        day = LightColorScheme.secondary,
        night = DarkColorScheme.secondary,
      ),
    onSecondary =
      ColorProvider(
        day = LightColorScheme.onSecondary,
        night = DarkColorScheme.onSecondary,
      ),
    secondaryContainer =
      ColorProvider(
        day = LightColorScheme.secondaryContainer,
        night = DarkColorScheme.secondaryContainer,
      ),
    onSecondaryContainer =
      ColorProvider(
        day = LightColorScheme.onSecondaryContainer,
        night = DarkColorScheme.onSecondaryContainer,
      ),
    tertiary =
      ColorProvider(
        day = LightColorScheme.tertiary,
        night = DarkColorScheme.tertiary,
      ),
    onTertiary =
      ColorProvider(
        day = LightColorScheme.onTertiary,
        night = DarkColorScheme.onTertiary,
      ),
    tertiaryContainer =
      ColorProvider(
        day = LightColorScheme.tertiaryContainer,
        night = DarkColorScheme.tertiaryContainer,
      ),
    onTertiaryContainer =
      ColorProvider(
        day = LightColorScheme.onTertiaryContainer,
        night = DarkColorScheme.onTertiaryContainer,
      ),
    error =
      ColorProvider(
        day = LightColorScheme.error,
        night = DarkColorScheme.error,
      ),
    errorContainer =
      ColorProvider(
        day = LightColorScheme.errorContainer,
        night = DarkColorScheme.errorContainer,
      ),
    onError =
      ColorProvider(
        day = LightColorScheme.onError,
        night = DarkColorScheme.onError,
      ),
    onErrorContainer =
      ColorProvider(
        day = LightColorScheme.onErrorContainer,
        night = DarkColorScheme.onErrorContainer,
      ),
    background =
      ColorProvider(
        day = LightColorScheme.background,
        night = DarkColorScheme.background,
      ),
    onBackground =
      ColorProvider(
        day = LightColorScheme.onBackground,
        night = DarkColorScheme.onBackground,
      ),
    surface =
      ColorProvider(
        day = LightColorScheme.surface,
        night = DarkColorScheme.surface,
      ),
    onSurface =
      ColorProvider(
        day = LightColorScheme.onSurface,
        night = DarkColorScheme.onSurface,
      ),
    surfaceVariant =
      ColorProvider(
        day = LightColorScheme.surfaceVariant,
        night = DarkColorScheme.surfaceVariant,
      ),
    onSurfaceVariant =
      ColorProvider(
        day = LightColorScheme.onSurfaceVariant,
        night = DarkColorScheme.onSurfaceVariant,
      ),
    outline =
      ColorProvider(
        day = LightColorScheme.outline,
        night = DarkColorScheme.outline,
      ),
    textColorPrimary =
      ColorProvider(
        day = LightColorScheme.onBackground,
        night = DarkColorScheme.onBackground,
      ),
    textColorSecondary =
      ColorProvider(
        day = LightColorScheme.onSecondaryContainer,
        night = DarkColorScheme.onSecondaryContainer,
      ),
    inverseOnSurface =
      ColorProvider(
        day = LightColorScheme.inverseOnSurface,
        night = DarkColorScheme.inverseOnSurface,
      ),
    inverseSurface =
      ColorProvider(
        day = LightColorScheme.inverseSurface,
        night = DarkColorScheme.inverseSurface,
      ),
    inversePrimary =
      ColorProvider(
        day = LightColorScheme.inversePrimary,
        night = DarkColorScheme.inversePrimary,
      ),
    inverseTextColorPrimary =
      ColorProvider(
        day = LightColorScheme.background,
        night = DarkColorScheme.background,
      ),
    inverseTextColorSecondary =
      ColorProvider(
        day = LightColorScheme.secondaryContainer,
        night = DarkColorScheme.secondaryContainer,
      ),
  )
