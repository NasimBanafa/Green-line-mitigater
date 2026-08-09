package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = IndigoAccent,
  secondary = CyanAccent,
  tertiary = EmeraldGreen,
  background = SlateDarkBg,
  surface = GlassSurface,
  surfaceContainer = GlassCard,
  onPrimary = TextPrimary,
  onSecondary = OledBlack,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  onSurfaceVariant = TextSecondary
)

private val LightColorScheme = lightColorScheme(
  primary = IndigoAccent,
  secondary = CyanAccent,
  tertiary = EmeraldGreen,
  background = SlateDarkBg,
  surface = GlassSurface,
  surfaceContainer = GlassCardBorder,
  onPrimary = TextPrimary,
  onSecondary = OledBlack,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  onSurfaceVariant = TextSecondary
)

@Composable
fun LineMaskTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
