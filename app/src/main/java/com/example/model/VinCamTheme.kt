package com.example.model

import androidx.compose.ui.graphics.Color

enum class VinCamThemeOption(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val accentColor: Color,
    val textPrimary: Color,
    val isDark: Boolean
) {
    VINCAM_PASTEL(
        displayName = "Vincam Pastel",
        primaryColor = Color(0xFFF98881), // Coral
        secondaryColor = Color(0xFFB8A5D6), // Lavender
        backgroundColor = Color(0xFFFFF8F0), // Cream Background
        surfaceColor = Color(0xFFFCF4EB), // Cream Surface
        accentColor = Color(0xFFFFB7B2), // Soft Pink
        textPrimary = Color(0xFF4A3B39), // Deep warm brown
        isDark = false
    )
}
