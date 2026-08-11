package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.VinCamThemeOption

@Composable
fun VinCamTheme(
    themeOption: VinCamThemeOption = VinCamThemeOption.GEOMETRIC_BALANCE,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = themeOption.primaryColor,
        secondary = themeOption.secondaryColor,
        tertiary = themeOption.accentColor,
        background = themeOption.backgroundColor,
        surface = themeOption.surfaceColor,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = themeOption.textPrimary,
        onSurface = themeOption.textPrimary,
        surfaceContainer = themeOption.surfaceColor,
        outline = themeOption.accentColor.copy(alpha = 0.4f)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
