package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CelestialPrimary,
    onPrimary = DarkBackground,
    secondary = CelestialSecondary,
    onSecondary = DarkBackground,
    tertiary = CelestialTertiary,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkText,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFF1F5F9),
    secondaryContainer = Color(0xFF0F172A),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiaryContainer = Color(0xFF1E293B),
    onTertiaryContainer = Color(0xFFF1F5F9),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBluePrimary,
    onPrimary = ParchmentBackground,
    secondary = SoftBlueSecondary,
    onSecondary = ParchmentBackground,
    tertiary = SoftBlueTertiary,
    onTertiary = ParchmentBackground,
    background = ParchmentBackground,
    onBackground = WarmText,
    surface = ParchmentSurface,
    onSurface = WarmText,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = WarmText,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF1E293B),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF334155),
    tertiaryContainer = Color(0xFFE2E8F0),
    onTertiaryContainer = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun BibleBridgeTheme(
    themePreference: String = "System",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themePreference) {
        "Dark" -> DarkColorScheme
        "Light" -> LightColorScheme
        else -> if (systemDark) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
