package com.trm.daylighter.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.trm.daylighter.core.ui.R
import com.trm.daylighter.core.ui.theme.AppFonts.googleFlex400
import com.trm.daylighter.core.ui.theme.AppFonts.googleFlex600

private val TYPOGRAPHY = Typography()
private const val featureSettings = "ss02, dlig"

private object AppFonts {
  val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))
  val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))
}

val Typography =
  Typography(
    displayLarge =
      TYPOGRAPHY.displayLarge.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    displayMedium =
      TYPOGRAPHY.displayMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    displaySmall =
      TYPOGRAPHY.displaySmall.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    headlineLarge =
      TYPOGRAPHY.headlineLarge.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    headlineMedium =
      TYPOGRAPHY.headlineMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    headlineSmall =
      TYPOGRAPHY.headlineSmall.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    titleLarge =
      TYPOGRAPHY.titleLarge.copy(fontFamily = googleFlex600, fontFeatureSettings = featureSettings),
    titleMedium =
      TYPOGRAPHY.titleMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    titleSmall =
      TYPOGRAPHY.titleSmall.copy(fontFamily = googleFlex600, fontFeatureSettings = featureSettings),
    bodyLarge =
      TYPOGRAPHY.bodyLarge.copy(fontFamily = googleFlex400, fontFeatureSettings = featureSettings),
    bodyMedium =
      TYPOGRAPHY.bodyMedium.copy(fontFamily = googleFlex400, fontFeatureSettings = featureSettings),
    bodySmall =
      TYPOGRAPHY.bodySmall.copy(fontFamily = googleFlex400, fontFeatureSettings = featureSettings),
    labelLarge =
      TYPOGRAPHY.labelLarge.copy(fontFamily = googleFlex600, fontFeatureSettings = featureSettings),
    labelMedium =
      TYPOGRAPHY.labelMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = featureSettings,
      ),
    labelSmall =
      TYPOGRAPHY.labelSmall.copy(fontFamily = googleFlex600, fontFeatureSettings = featureSettings),
  )
