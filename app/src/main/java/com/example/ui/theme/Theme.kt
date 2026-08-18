package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PurpleScoreCard,
    onPrimary = PurpleDeep,
    primaryContainer = PurplePrimary,
    onPrimaryContainer = Color.White,
    secondary = RoseScoreCard,
    onSecondary = RoseDark,
    secondaryContainer = RosePrimary,
    onSecondaryContainer = Color.White,
    tertiary = GoldCelebration,
    onTertiary = Color.Black,
    background = Color(0xFF1D1B20),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF2B2830),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = BoldThemeBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleLightContainer,
    onPrimaryContainer = PurpleDeep,
    secondary = RosePrimary,
    onSecondary = Color.White,
    secondaryContainer = RoseLightContainer,
    onSecondaryContainer = RoseDark,
    tertiary = GoldCelebration,
    onTertiary = Color.Black,
    background = BoldThemeBackground,
    onBackground = BoldThemeTextPrimary,
    surface = BoldThemeSurface,
    onSurface = BoldThemeTextPrimary,
    surfaceVariant = BoldThemeSurfaceVariant,
    onSurfaceVariant = BoldThemeTextMuted,
    outline = BoldThemeBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to bold high-contrast clean theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

