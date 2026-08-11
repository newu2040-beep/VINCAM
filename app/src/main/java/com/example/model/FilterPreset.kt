package com.example.model

import androidx.compose.ui.graphics.ColorMatrix

enum class FilterCategory(val label: String) {
    RETRO("RETRO"),
    FILM("FILM"),
    CINEMA("CINEMA"),
    AESTHETIC("AESTHETIC")
}

data class FilterPreset(
    val id: String,
    val name: String,
    val category: FilterCategory,
    val description: String,
    val colorMatrix: FloatArray,
    val defaultIntensity: Float = 1.0f,
    val temperatureOffset: Float = 0.0f,
    val exposureOffset: Float = 0.0f,
    val contrastOffset: Float = 0.0f,
    val saturationOffset: Float = 0.0f,
    val grainLevel: Float = 0.0f,
    val vignetteLevel: Float = 0.0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilterPreset) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun getAdjustedColorMatrix(intensity: Float): ColorMatrix {
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val identity = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        val blended = FloatArray(20) { i ->
            identity[i] + (colorMatrix[i] - identity[i]) * clampedIntensity
        }
        return ColorMatrix(blended)
    }

    companion object {
        val NONE = FilterPreset(
            id = "none",
            name = "Normal",
            category = FilterCategory.RETRO,
            description = "Natural clean camera output",
            colorMatrix = floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val ALL_PRESETS: List<FilterPreset> = listOf(
            NONE,
            // RETRO CATEGORY
            FilterPreset(
                id = "retro_vintage_01",
                name = "Vintage 01",
                category = FilterCategory.RETRO,
                description = "Warm analog paper tones with crushed blacks",
                colorMatrix = floatArrayOf(
                    1.15f, 0.05f, -0.05f, 0f, 15f,
                    0.05f, 1.05f, 0.00f, 0f, 10f,
                    -0.05f, 0.05f, 0.85f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                ),
                temperatureOffset = 15f,
                contrastOffset = 0.1f,
                grainLevel = 0.25f
            ),
            FilterPreset(
                id = "retro_vintage_02",
                name = "Vintage 02",
                category = FilterCategory.RETRO,
                description = "Sepia golden glow with muted blues",
                colorMatrix = floatArrayOf(
                    1.2f, 0.1f, 0.0f, 0f, 20f,
                    0.1f, 1.1f, 0.0f, 0f, 12f,
                    0.0f, 0.1f, 0.75f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ),
                temperatureOffset = 25f,
                grainLevel = 0.35f
            ),
            FilterPreset(
                id = "retro_90s_camera",
                name = "90s Camera",
                category = FilterCategory.RETRO,
                description = "Vibrant 90s point-and-shoot flash look",
                colorMatrix = floatArrayOf(
                    1.25f, -0.05f, -0.05f, 0f, 18f,
                    -0.05f, 1.15f, -0.05f, 0f, 8f,
                    -0.05f, -0.05f, 1.10f, 0f, -2f,
                    0f, 0f, 0f, 1f, 0f
                ),
                exposureOffset = 0.05f,
                contrastOffset = 0.15f
            ),
            FilterPreset(
                id = "retro_2000s_digital",
                name = "2000s Digital",
                category = FilterCategory.RETRO,
                description = "Early compact digital sensor tone with cool highlights",
                colorMatrix = floatArrayOf(
                    0.95f, 0.05f, 0.10f, 0f, 5f,
                    0.00f, 1.05f, 0.05f, 0f, 5f,
                    0.05f, 0.05f, 1.20f, 0f, 15f,
                    0.0f, 0.0f, 0.0f, 1f, 0f
                ),
                temperatureOffset = -15f
            ),
            FilterPreset(
                id = "retro_disposable",
                name = "Disposable",
                category = FilterCategory.RETRO,
                description = "High-contrast single-use camera look",
                colorMatrix = floatArrayOf(
                    1.30f, -0.10f, 0.00f, 0f, 22f,
                    -0.05f, 1.10f, -0.05f, 0f, 10f,
                    0.00f, -0.10f, 0.90f, 0f, -12f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = 0.25f,
                grainLevel = 0.40f
            ),
            FilterPreset(
                id = "retro_old_ccd",
                name = "Old CCD",
                category = FilterCategory.RETRO,
                description = "CCD sensor color profile with saturated primary hues",
                colorMatrix = floatArrayOf(
                    1.20f, -0.08f, -0.02f, 0f, 10f,
                    -0.05f, 1.25f, -0.08f, 0f, 8f,
                    -0.02f, -0.08f, 1.15f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                ),
                saturationOffset = 0.20f
            ),
            FilterPreset(
                id = "retro_flash",
                name = "Retro Flash",
                category = FilterCategory.RETRO,
                description = "Direct flash pop with rich dark shadows",
                colorMatrix = floatArrayOf(
                    1.35f, -0.12f, -0.12f, 0f, 30f,
                    -0.10f, 1.20f, -0.10f, 0f, 15f,
                    -0.10f, -0.10f, 1.05f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ),
                exposureOffset = 0.10f,
                contrastOffset = 0.30f
            ),
            FilterPreset(
                id = "retro_faded_film",
                name = "Faded Film",
                category = FilterCategory.RETRO,
                description = "Sun-bleached photo album nostalgia",
                colorMatrix = floatArrayOf(
                    1.05f, 0.10f, 0.05f, 0f, 25f,
                    0.08f, 0.98f, 0.08f, 0f, 22f,
                    0.05f, 0.10f, 0.88f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = -0.15f
            ),

            // FILM CATEGORY
            FilterPreset(
                id = "film_soft",
                name = "Soft Film",
                category = FilterCategory.FILM,
                description = "Gentle highlight roll-off and airy pastel tones",
                colorMatrix = floatArrayOf(
                    1.02f, 0.04f, 0.02f, 0f, 12f,
                    0.02f, 1.04f, 0.02f, 0f, 12f,
                    0.02f, 0.04f, 1.02f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = -0.08f
            ),
            FilterPreset(
                id = "film_classic",
                name = "Classic Film",
                category = FilterCategory.FILM,
                description = "Balanced 35mm emulsion rendering",
                colorMatrix = floatArrayOf(
                    1.10f, -0.02f, 0.00f, 0f, 8f,
                    0.00f, 1.06f, 0.00f, 0f, 6f,
                    -0.02f, 0.02f, 0.96f, 0f, -2f,
                    0f, 0f, 0f, 1f, 0f
                ),
                grainLevel = 0.20f
            ),
            FilterPreset(
                id = "film_warm",
                name = "Warm Film",
                category = FilterCategory.FILM,
                description = "Sunset amber warmth and golden skin tones",
                colorMatrix = floatArrayOf(
                    1.18f, 0.06f, -0.04f, 0f, 18f,
                    0.04f, 1.08f, -0.02f, 0f, 12f,
                    -0.04f, 0.02f, 0.88f, 0f, -8f,
                    0f, 0f, 0f, 1f, 0f
                ),
                temperatureOffset = 20f
            ),
            FilterPreset(
                id = "film_cool",
                name = "Cool Film",
                category = FilterCategory.FILM,
                description = "Cinematic slate blues and icy shadows",
                colorMatrix = floatArrayOf(
                    0.94f, 0.02f, 0.08f, 0f, -4f,
                    0.02f, 1.00f, 0.08f, 0f, 2f,
                    0.06f, 0.06f, 1.18f, 0f, 16f,
                    0f, 0f, 0f, 1f, 0f
                ),
                temperatureOffset = -20f
            ),
            FilterPreset(
                id = "film_grain",
                name = "Grain Film",
                category = FilterCategory.FILM,
                description = "High ISO silver halide texture",
                colorMatrix = floatArrayOf(
                    1.08f, -0.04f, -0.04f, 0f, 10f,
                    -0.02f, 1.04f, -0.02f, 0f, 10f,
                    -0.04f, -0.02f, 0.98f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                ),
                grainLevel = 0.55f
            ),
            FilterPreset(
                id = "film_matte",
                name = "Matte Film",
                category = FilterCategory.FILM,
                description = "Flat black point for editorial aesthetic",
                colorMatrix = floatArrayOf(
                    0.98f, 0.05f, 0.05f, 0f, 28f,
                    0.05f, 0.98f, 0.05f, 0f, 28f,
                    0.05f, 0.05f, 0.98f, 0f, 28f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = -0.20f
            ),
            FilterPreset(
                id = "film_negative",
                name = "Negative Film",
                category = FilterCategory.FILM,
                description = "Stylized inverted emulsion look",
                colorMatrix = floatArrayOf(
                    -0.85f, 0.15f, 0.15f, 0f, 235f,
                    0.15f, -0.85f, 0.15f, 0f, 235f,
                    0.15f, 0.15f, -0.85f, 0f, 235f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            FilterPreset(
                id = "film_instant",
                name = "Instant Film",
                category = FilterCategory.FILM,
                description = "Square instant camera chemical tint",
                colorMatrix = floatArrayOf(
                    1.12f, 0.08f, -0.02f, 0f, 16f,
                    0.02f, 1.10f, 0.05f, 0f, 14f,
                    -0.05f, 0.05f, 0.85f, 0f, -4f,
                    0f, 0f, 0f, 1f, 0f
                ),
                vignetteLevel = 0.30f
            ),

            // CINEMA CATEGORY
            FilterPreset(
                id = "cinema_warm",
                name = "Cinema Warm",
                category = FilterCategory.CINEMA,
                description = "Teal and orange cinematic color grade",
                colorMatrix = floatArrayOf(
                    1.22f, -0.05f, -0.10f, 0f, 15f,
                    -0.02f, 1.05f, 0.02f, 0f, 5f,
                    -0.10f, 0.05f, 1.15f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = 0.12f
            ),
            FilterPreset(
                id = "cinema_cool",
                name = "Cinema Cool",
                category = FilterCategory.CINEMA,
                description = "Nordic thriller slate color profile",
                colorMatrix = floatArrayOf(
                    0.90f, 0.05f, 0.05f, 0f, -6f,
                    0.02f, 1.05f, 0.08f, 0f, 4f,
                    0.05f, 0.05f, 1.25f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ),
                temperatureOffset = -25f
            ),
            FilterPreset(
                id = "cinema_moody",
                name = "Moody Cinema",
                category = FilterCategory.CINEMA,
                description = "Deep shadows and saturated highlights",
                colorMatrix = floatArrayOf(
                    1.15f, -0.08f, -0.08f, 0f, -8f,
                    -0.08f, 1.10f, -0.08f, 0f, -8f,
                    -0.08f, -0.08f, 1.05f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = 0.30f,
                vignetteLevel = 0.40f
            ),
            FilterPreset(
                id = "cinema_night",
                name = "Night Cinema",
                category = FilterCategory.CINEMA,
                description = "Neon city low-light enhancement",
                colorMatrix = floatArrayOf(
                    1.25f, -0.10f, 0.05f, 0f, 10f,
                    -0.10f, 1.10f, 0.10f, 0f, 0f,
                    0.05f, 0.05f, 1.30f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                ),
                exposureOffset = 0.15f
            ),
            FilterPreset(
                id = "cinema_faded",
                name = "Faded Cinema",
                category = FilterCategory.CINEMA,
                description = "Retro widescreen film stock",
                colorMatrix = floatArrayOf(
                    1.05f, 0.08f, 0.02f, 0f, 20f,
                    0.05f, 1.02f, 0.05f, 0f, 18f,
                    0.02f, 0.08f, 0.95f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            FilterPreset(
                id = "cinema_golden",
                name = "Golden Cinema",
                category = FilterCategory.CINEMA,
                description = "Golden hour anamorphic warmth",
                colorMatrix = floatArrayOf(
                    1.30f, 0.10f, -0.12f, 0f, 25f,
                    0.05f, 1.15f, -0.08f, 0f, 15f,
                    -0.10f, -0.05f, 0.82f, 0f, -15f,
                    0f, 0f, 0f, 1f, 0f
                ),
                temperatureOffset = 30f
            ),

            // AESTHETIC CATEGORY
            FilterPreset(
                id = "aesthetic_latte",
                name = "Latte",
                category = FilterCategory.AESTHETIC,
                description = "Creamy espresso tones and soft contrast",
                colorMatrix = floatArrayOf(
                    1.12f, 0.08f, 0.00f, 0f, 18f,
                    0.06f, 1.04f, 0.02f, 0f, 14f,
                    0.00f, 0.04f, 0.90f, 0f, 5f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            FilterPreset(
                id = "aesthetic_cream",
                name = "Cream",
                category = FilterCategory.AESTHETIC,
                description = "Milky soft brightness and warm whites",
                colorMatrix = floatArrayOf(
                    1.08f, 0.06f, 0.04f, 0f, 22f,
                    0.04f, 1.06f, 0.04f, 0f, 20f,
                    0.04f, 0.06f, 1.00f, 0f, 22f,
                    0f, 0f, 0f, 1f, 0f
                ),
                exposureOffset = 0.08f
            ),
            FilterPreset(
                id = "aesthetic_sakura",
                name = "Sakura",
                category = FilterCategory.AESTHETIC,
                description = "Soft pink tint and romantic highlights",
                colorMatrix = floatArrayOf(
                    1.20f, 0.05f, 0.10f, 0f, 20f,
                    0.05f, 1.02f, 0.08f, 0f, 8f,
                    0.08f, 0.05f, 1.10f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            FilterPreset(
                id = "aesthetic_matcha",
                name = "Matcha",
                category = FilterCategory.AESTHETIC,
                description = "Calm herbal green tint and natural shadows",
                colorMatrix = floatArrayOf(
                    1.00f, 0.08f, -0.02f, 0f, 4f,
                    0.08f, 1.18f, 0.02f, 0f, 16f,
                    -0.02f, 0.04f, 0.96f, 0f, 2f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            FilterPreset(
                id = "aesthetic_peach",
                name = "Peach",
                category = FilterCategory.AESTHETIC,
                description = "Warm orange-pink glow and flattering skin tones",
                colorMatrix = floatArrayOf(
                    1.22f, 0.08f, -0.04f, 0f, 24f,
                    0.05f, 1.08f, -0.02f, 0f, 14f,
                    -0.04f, 0.02f, 0.92f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            FilterPreset(
                id = "aesthetic_soft_pastel",
                name = "Soft Pastel",
                category = FilterCategory.AESTHETIC,
                description = "Dreamy low saturation and lifted shadows",
                colorMatrix = floatArrayOf(
                    1.02f, 0.06f, 0.06f, 0f, 25f,
                    0.06f, 1.02f, 0.06f, 0f, 25f,
                    0.06f, 0.06f, 1.02f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                ),
                saturationOffset = -0.15f
            ),
            FilterPreset(
                id = "aesthetic_dreamy",
                name = "Dreamy",
                category = FilterCategory.AESTHETIC,
                description = "Ethereal glow with soft focus contrast",
                colorMatrix = floatArrayOf(
                    1.12f, 0.08f, 0.08f, 0f, 18f,
                    0.08f, 1.10f, 0.08f, 0f, 18f,
                    0.08f, 0.08f, 1.15f, 0f, 22f,
                    0f, 0f, 0f, 1f, 0f
                ),
                contrastOffset = -0.12f
            ),
            FilterPreset(
                id = "aesthetic_minimal",
                name = "Minimal",
                category = FilterCategory.AESTHETIC,
                description = "Clean desaturated modern architectural look",
                colorMatrix = floatArrayOf(
                    0.95f, 0.02f, 0.02f, 0f, 6f,
                    0.02f, 0.95f, 0.02f, 0f, 6f,
                    0.02f, 0.02f, 0.95f, 0f, 6f,
                    0f, 0f, 0f, 1f, 0f
                ),
                saturationOffset = -0.30f
            )
        )
    }
}
