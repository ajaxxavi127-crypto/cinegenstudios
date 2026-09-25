package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CineDarkColorScheme = darkColorScheme(
    primary = CineCyanPrimary,
    onPrimary = CineCyanOnPrimary,
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = Color(0xFF80F2FF),
    secondary = CineVioletSecondary,
    onSecondary = CineVioletOnSecondary,
    secondaryContainer = Color(0xFF3F1D7F),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = CineAmberTertiary,
    onTertiary = CineAmberOnTertiary,
    tertiaryContainer = Color(0xFF5B3D00),
    onTertiaryContainer = Color(0xFFFFDF9E),
    background = CineBackgroundDark,
    onBackground = CineTextPrimary,
    surface = CineSurfaceDark,
    onSurface = CineTextPrimary,
    surfaceVariant = CineSurfaceContainer,
    onSurfaceVariant = CineTextSecondary,
    surfaceContainer = CineSurfaceContainer,
    surfaceContainerHigh = CineSurfaceContainerHigh,
    outline = CineSurfaceBorder,
    error = CineError,
    onError = Color.White
)

@Composable
fun CineGenStudioTheme(
    darkTheme: Boolean = true, // Default to sleek cinematic dark mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CineDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CineGenStudioTheme(darkTheme = true, content = content)
}
