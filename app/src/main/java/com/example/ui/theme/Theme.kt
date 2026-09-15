package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
      primary = Color(0xFF818CF8),
      onPrimary = Color(0xFF0F172A),
      primaryContainer = Color(0xFF2E335B),
      onPrimaryContainer = Color(0xFFE0E7FF),
      secondary = Color(0xFF94A3B8),
      onSecondary = Color(0xFF0F172A),
      secondaryContainer = Color(0xFF1E293B),
      onSecondaryContainer = Color(0xFFF1F5F9),
      tertiary = Color(0xFFF472B6),
      onTertiary = Color(0xFF4A0420),
      background = Color(0xFF0B0F19),
      onBackground = Color(0xFFF1F5F9),
      surface = Color(0xFF151D2A),
      onSurface = Color(0xFFF1F5F9),
      surfaceVariant = Color(0xFF1E293B),
      onSurfaceVariant = Color(0xFF94A3B8),
      outline = Color(0xFF6366F1),
      outlineVariant = Color(0xFF2A384C),
      error = Color(0xFFF87171),
      onError = Color(0xFF450A0A),
      errorContainer = Color(0xFF4A1818),
      onErrorContainer = Color(0xFFFECACA)
  )

private val LightColorScheme =
  lightColorScheme(
      primary = Color(0xFF4F46E5),
      onPrimary = Color.White,
      primaryContainer = Color(0xFFEEF2FF),
      onPrimaryContainer = Color(0xFF312E81),
      secondary = Color(0xFF475569),
      onSecondary = Color.White,
      secondaryContainer = Color(0xFFF1F5F9),
      onSecondaryContainer = Color(0xFF0F172A),
      tertiary = Color(0xFFDB2777),
      onTertiary = Color.White,
      background = Color(0xFFF1F5F9),
      onBackground = Color(0xFF0F172A),
      surface = Color(0xFFFFFFFF),
      onSurface = Color(0xFF0F172A),
      surfaceVariant = Color(0xFFF8FAFC),
      onSurfaceVariant = Color(0xFF475569),
      outline = Color(0xFF94A3B8),
      outlineVariant = Color(0xFFCBD5E1)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default so our custom, crisp high-contrast theme shines consistently
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
