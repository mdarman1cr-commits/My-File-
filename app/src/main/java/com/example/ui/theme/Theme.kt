package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = SkyPrimaryDark,
  onPrimary = DarkBackground,
  primaryContainer = DarkSurfaceVariant,
  onPrimaryContainer = SkyPrimaryDark,
  secondary = IndigoSecondaryDark,
  onSecondary = DarkBackground,
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = IndigoSecondaryDark,
  tertiary = EmeraldTertiaryDark,
  onTertiary = DarkBackground,
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkOnSurface,
  outline = DarkOutline,
  error = RoseError
)

private val LightColorScheme = lightColorScheme(
  primary = SkyPrimaryLight,
  onPrimary = LightSurface,
  primaryContainer = LightSurfaceVariant,
  onPrimaryContainer = SkyPrimaryLight,
  secondary = IndigoSecondaryLight,
  onSecondary = LightSurface,
  secondaryContainer = LightSurfaceVariant,
  onSecondaryContainer = IndigoSecondaryLight,
  tertiary = EmeraldTertiaryLight,
  onTertiary = LightSurface,
  background = LightBackground,
  onBackground = LightOnBackground,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightOnSurface,
  outline = LightOutline,
  error = RoseError
)

enum class ThemeMode {
  SYSTEM, LIGHT, DARK
}

@Composable
fun MyApplicationTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
