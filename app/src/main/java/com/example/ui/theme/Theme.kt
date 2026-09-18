package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaPrimaryDark,
    onPrimary = PaperBackgroundDark,
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = TerracottaOnContainerDark,
    secondary = InkSecondaryDark,
    onSecondary = PaperBackgroundDark,
    background = PaperBackgroundDark,
    onBackground = InkPrimaryDark,
    surface = PaperSurfaceDark,
    onSurface = InkPrimaryDark,
    surfaceVariant = PaperSurfaceVariantDark,
    onSurfaceVariant = InkSecondaryDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimaryLight,
    onPrimary = PaperSurfaceLight,
    primaryContainer = TerracottaContainerLight,
    onPrimaryContainer = TerracottaOnContainerLight,
    secondary = InkSecondaryLight,
    onSecondary = PaperSurfaceLight,
    background = PaperBackgroundLight,
    onBackground = InkPrimaryLight,
    surface = PaperSurfaceLight,
    onSurface = InkPrimaryLight,
    surfaceVariant = PaperSurfaceVariantLight,
    onSurfaceVariant = InkSecondaryLight,
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

