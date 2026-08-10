package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkModePrimaryBlue,
    onPrimary = Color.Black,
    primaryContainer = DarkElevatedSurface,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkAccentBlue,
    onSecondary = Color.Black,
    tertiary = DarkAccentBlue,
    background = DarkMainBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderColor,
    outlineVariant = DarkBorderColor,
    error = DarkErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlueBg,
    onPrimaryContainer = DarkPrimaryBlue,
    secondary = DarkPrimaryBlue,
    onSecondary = Color.White,
    tertiary = SkyBlueAccent,
    background = MainBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = LightBlueBg,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = DividerColor,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = false, // Keep consistent Digital Tool branding colors
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
