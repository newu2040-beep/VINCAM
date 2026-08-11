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
    GEOMETRIC_BALANCE(
        displayName = "Geometric Balance",
        primaryColor = Color(0xFFFF6321),
        secondaryColor = Color(0xFF2A2420),
        backgroundColor = Color(0xFF0D0D0D),
        surfaceColor = Color(0xFF1A1A1A),
        accentColor = Color(0xFFFF6321),
        textPrimary = Color(0xFFF5F2ED),
        isDark = true
    ),
    VINTAGE_CREAM(
        displayName = "Vintage Cream",
        primaryColor = Color(0xFFC88A4C),
        secondaryColor = Color(0xFF4E7C77),
        backgroundColor = Color(0xFF1E1B18),
        surfaceColor = Color(0xFF2B2622),
        accentColor = Color(0xFFE5A65D),
        textPrimary = Color(0xFFF7F1E5),
        isDark = true
    ),
    RETRO_BROWN(
        displayName = "Retro Brown",
        primaryColor = Color(0xFFA66438),
        secondaryColor = Color(0xFFD9A05B),
        backgroundColor = Color(0xFF191412),
        surfaceColor = Color(0xFF27201C),
        accentColor = Color(0xFFE89B57),
        textPrimary = Color(0xFFFAECE1),
        isDark = true
    ),
    COFFEE(
        displayName = "Coffee",
        primaryColor = Color(0xFF8C5835),
        secondaryColor = Color(0xFFC59B73),
        backgroundColor = Color(0xFF14100E),
        surfaceColor = Color(0xFF221B17),
        accentColor = Color(0xFFDFA874),
        textPrimary = Color(0xFFF5EBE1),
        isDark = true
    ),
    FILM_GREEN(
        displayName = "Film Green",
        primaryColor = Color(0xFF486E56),
        secondaryColor = Color(0xFF8CAE96),
        backgroundColor = Color(0xFF121A15),
        surfaceColor = Color(0xFF1D2821),
        accentColor = Color(0xFF6DA27E),
        textPrimary = Color(0xFFEBF5EE),
        isDark = true
    ),
    DUSTY_PINK(
        displayName = "Dusty Pink",
        primaryColor = Color(0xFFB86B77),
        secondaryColor = Color(0xFFDDA6B0),
        backgroundColor = Color(0xFF1C1315),
        surfaceColor = Color(0xFF291E21),
        accentColor = Color(0xFFE88B9A),
        textPrimary = Color(0xFFFBECEF),
        isDark = true
    ),
    LAVENDER(
        displayName = "Lavender",
        primaryColor = Color(0xFF7E6B9E),
        secondaryColor = Color(0xFFB4A6CD),
        backgroundColor = Color(0xFF16141D),
        surfaceColor = Color(0xFF23202E),
        accentColor = Color(0xFF9E87C6),
        textPrimary = Color(0xFFF3EFFF),
        isDark = true
    ),
    SAGE(
        displayName = "Sage",
        primaryColor = Color(0xFF6B8B7B),
        secondaryColor = Color(0xFFA5C4B4),
        backgroundColor = Color(0xFF131A17),
        surfaceColor = Color(0xFF202A25),
        accentColor = Color(0xFF85AB97),
        textPrimary = Color(0xFFEDF7F2),
        isDark = true
    ),
    MATCHA(
        displayName = "Matcha",
        primaryColor = Color(0xFF708B48),
        secondaryColor = Color(0xFFB1C98D),
        backgroundColor = Color(0xFF141A10),
        surfaceColor = Color(0xFF21291B),
        accentColor = Color(0xFF8EAC5D),
        textPrimary = Color(0xFFF1F8E9),
        isDark = true
    ),
    PEACH(
        displayName = "Peach",
        primaryColor = Color(0xFFD67D65),
        secondaryColor = Color(0xFFF3B49F),
        backgroundColor = Color(0xFF1E1412),
        surfaceColor = Color(0xFF2E1F1B),
        accentColor = Color(0xFFF0957D),
        textPrimary = Color(0xFFFDF0ED),
        isDark = true
    ),
    SUNSET(
        displayName = "Sunset",
        primaryColor = Color(0xFFD85D38),
        secondaryColor = Color(0xFFE99649),
        backgroundColor = Color(0xFF1C110F),
        surfaceColor = Color(0xFF2B1A17),
        accentColor = Color(0xFFF27A4B),
        textPrimary = Color(0xFFFDEEEA),
        isDark = true
    ),
    MIDNIGHT(
        displayName = "Midnight",
        primaryColor = Color(0xFF3F638A),
        secondaryColor = Color(0xFF7AA5C8),
        backgroundColor = Color(0xFF0F141B),
        surfaceColor = Color(0xFF17202B),
        accentColor = Color(0xFF5382B3),
        textPrimary = Color(0xFFECF3FA),
        isDark = true
    ),
    GRAPHITE(
        displayName = "Graphite",
        primaryColor = Color(0xFF787D85),
        secondaryColor = Color(0xFFB0B5BC),
        backgroundColor = Color(0xFF141517),
        surfaceColor = Color(0xFF202226),
        accentColor = Color(0xFF9AA0A8),
        textPrimary = Color(0xFFF3F4F6),
        isDark = true
    ),
    SOFT_BLUE(
        displayName = "Soft Blue",
        primaryColor = Color(0xFF5585A3),
        secondaryColor = Color(0xFF98C1DA),
        backgroundColor = Color(0xFF11171B),
        surfaceColor = Color(0xFF1C262C),
        accentColor = Color(0xFF6BA3C7),
        textPrimary = Color(0xFFEEF6FC),
        isDark = true
    )
}
