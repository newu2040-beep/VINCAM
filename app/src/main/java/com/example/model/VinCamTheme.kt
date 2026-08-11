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
    ),
    COTTON_CANDY(
        displayName = "Cotton Candy",
        primaryColor = Color(0xFF72C2F1), // Soft Sky Blue
        secondaryColor = Color(0xFFF49AC2), // Bubblegum Pink
        backgroundColor = Color(0xFFF5F9FF), // Soft Cloud
        surfaceColor = Color(0xFFEBF3FE), // Pale Ice
        accentColor = Color(0xFFD2B4DE), // Candy Lavender
        textPrimary = Color(0xFF2C3E50), // Deep Slate
        isDark = false
    ),
    MINT_MATCHA(
        displayName = "Mint Matcha",
        primaryColor = Color(0xFF52B788), // Pastel Mint Green
        secondaryColor = Color(0xFFA2D5C6), // Sage
        backgroundColor = Color(0xFFFAF9F5), // Warm Vanilla
        surfaceColor = Color(0xFFF0FAF6), // Pale Tea
        accentColor = Color(0xFF74C69D), // Fresh Mint
        textPrimary = Color(0xFF2D3A34), // Deep Forest
        isDark = false
    ),
    PEACH_FUZZ(
        displayName = "Peach Fuzz",
        primaryColor = Color(0xFFFF8A65), // Soft Peach
        secondaryColor = Color(0xFFFFC1A1), // Apricot Cream
        backgroundColor = Color(0xFFFFF9F5), // Soft Ivory
        surfaceColor = Color(0xFFFFF2E8), // Warm Peach
        accentColor = Color(0xFFFFAB91), // Coral Fuzz
        textPrimary = Color(0xFF3E2723), // Warm Espresso
        isDark = false
    ),
    LAVENDER_MIST(
        displayName = "Lavender Mist",
        primaryColor = Color(0xFFB39DDB), // Pastel Lilac
        secondaryColor = Color(0xFF9FA8DA), // Soft Periwinkle
        backgroundColor = Color(0xFFFAFAFF), // Cloud Cream
        surfaceColor = Color(0xFFF3E5F5), // Violet Ice
        accentColor = Color(0xFF9575CD), // Lavender
        textPrimary = Color(0xFF311B92), // Deep Violet
        isDark = false
    ),
    HONEY_BUTTER(
        displayName = "Honey Butter",
        primaryColor = Color(0xFFFFB300), // Warm Honey
        secondaryColor = Color(0xFFFFCA28), // Butter
        backgroundColor = Color(0xFFFFFDE7), // Warm Linen
        surfaceColor = Color(0xFFFFF9C4), // Cream
        accentColor = Color(0xFFFFE082), // Soft Gold
        textPrimary = Color(0xFF3E2723), // Deep Mocha
        isDark = false
    ),
    SAKURA_PETAL(
        displayName = "Sakura Petal",
        primaryColor = Color(0xFFEC407A), // Sakura Pink
        secondaryColor = Color(0xFFF48FB1), // Rose Gold
        backgroundColor = Color(0xFFFFF5F8), // Pearl White
        surfaceColor = Color(0xFFFCE4EC), // Pale Blossom
        accentColor = Color(0xFFF8BBD0), // Soft Petal
        textPrimary = Color(0xFF4A148C), // Deep Plum
        isDark = false
    ),
    SEA_BREEZE(
        displayName = "Sea Breeze",
        primaryColor = Color(0xFF26C6DA), // Aquamarine
        secondaryColor = Color(0xFF80DEEA), // Foam Blue
        backgroundColor = Color(0xFFF0FBFD), // Soft Salt
        surfaceColor = Color(0xFFE0F7FA), // Soft Tide
        accentColor = Color(0xFF4DD0E1), // Ocean Cyan
        textPrimary = Color(0xFF004D40), // Ocean Teal
        isDark = false
    ),
    STRAWBERRY_MILK(
        displayName = "Strawberry Milk",
        primaryColor = Color(0xFFFF4081), // Strawberry Pink
        secondaryColor = Color(0xFFFF80AB), // Creamy Vanilla
        backgroundColor = Color(0xFFFFF0F5), // Soft Milk
        surfaceColor = Color(0xFFFFE4E1), // Pale Rose
        accentColor = Color(0xFFFF80AB), // Berry Blush
        textPrimary = Color(0xFF880E4F), // Deep Berry
        isDark = false
    ),
    LILAC_SUNSET(
        displayName = "Lilac Sunset",
        primaryColor = Color(0xFFFF7043), // Sunset Coral
        secondaryColor = Color(0xFFAB47BC), // Dusk Violet
        backgroundColor = Color(0xFFFFF3E0), // Sunset Warmth
        surfaceColor = Color(0xFFF3E5F5), // Twilight Glow
        accentColor = Color(0xFFFFB74D), // Dusk Amber
        textPrimary = Color(0xFF4A148C), // Dark Sunset
        isDark = false
    ),
    PISTACHIO_CREAM(
        displayName = "Pistachio Cream",
        primaryColor = Color(0xFF8BC34A), // Pistachio Green
        secondaryColor = Color(0xFFC5E1A5), // Oat Milk
        backgroundColor = Color(0xFFF9FBE7), // Soft Oat
        surfaceColor = Color(0xFFF0F4C3), // Pale Pistachio
        accentColor = Color(0xFFAED581), // Fresh Olive
        textPrimary = Color(0xFF33691E), // Deep Olive
        isDark = false
    ),
    VANILLA_LATTE(
        displayName = "Vanilla Latte",
        primaryColor = Color(0xFFD7CCC8), // Warm Coffee Cream
        secondaryColor = Color(0xFFA1887F), // Espresso Tan
        backgroundColor = Color(0xFFEFEBE9), // Soft Linen
        surfaceColor = Color(0xFFD7CCC8), // Warm Oat
        accentColor = Color(0xFF8D6E63), // Cocoa
        textPrimary = Color(0xFF3E2723), // Deep Coffee
        isDark = false
    ),
    CLOUD_NINE(
        displayName = "Cloud Nine",
        primaryColor = Color(0xFF81D4FA), // Powder Blue
        secondaryColor = Color(0xFFB3E5FC), // Soft Cyan
        backgroundColor = Color(0xFFF1F8E9), // Cloud Cream
        surfaceColor = Color(0xFFE1F5FE), // Light Breeze
        accentColor = Color(0xFF4FC3F7), // Sky Accent
        textPrimary = Color(0xFF01579B), // Deep Navy
        isDark = false
    ),
    BUBBLEGUM_SKY(
        displayName = "Bubblegum Sky",
        primaryColor = Color(0xFFFF80AB), // Bubblegum Pink
        secondaryColor = Color(0xFF80D8FF), // Pastel Cyan
        backgroundColor = Color(0xFFFFF4F8), // Pale Rose
        surfaceColor = Color(0xFFFFE4EC), // Sweet Pink
        accentColor = Color(0xFF82B1FF), // Soft Sky
        textPrimary = Color(0xFF880E4F), // Deep Plum
        isDark = false
    ),
    VELVET_PASTEL_NIGHT(
        displayName = "Velvet Night",
        primaryColor = Color(0xFFFF8A80), // Rose Gold Glow
        secondaryColor = Color(0xFFB39DDB), // Soft Lilac Night
        backgroundColor = Color(0xFF1B1822), // Midnight Velvet
        surfaceColor = Color(0xFF272233), // Deep Violet Surface
        accentColor = Color(0xFFEA80FC), // Glow Orchid
        textPrimary = Color(0xFFFAF0E6), // Soft Cream White
        isDark = true
    )
}
