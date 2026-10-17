package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IndustrialAmber,
    onPrimary = Color(0xFF1E1000),
    primaryContainer = IndustrialAmberDark,
    onPrimaryContainer = Color(0xFFFFD180),
    secondary = SteelBlue,
    onSecondary = Color(0xFF002238),
    secondaryContainer = SteelBlueDark,
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = EpoxyGreen,
    onTertiary = Color(0xFF002818),
    tertiaryContainer = EpoxyGreenDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = SteelDarkBg,
    onBackground = TextPrimary,
    surface = SteelDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = SteelCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SteelCardBorder,
    outlineVariant = Color(0xFF1E293B),
    error = ScrapRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IndustrialAmberDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4E2600),
    secondary = SteelBlueDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = EpoxyGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = ScrapRed,
    onError = Color.White
)

@Composable
fun TalaiSteelTheme(
    darkTheme: Boolean = true, // Industrial dark by default for factory floor visibility
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
