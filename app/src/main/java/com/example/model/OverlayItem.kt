package com.example.model

enum class OverlayType {
    TEXT,
    EMOJI,
    STICKER,
    FRAME,
    PNG
}

data class OverlayItem(
    val id: String,
    val type: OverlayType,
    val textContent: String = "",
    val assetNameOrEmoji: String = "",
    val fontStyle: String = "Retro Vintage",
    val fontSizeSp: Float = 28f,
    val colorHex: String = "#FFFFFF",
    val strokeColorHex: String = "#000000",
    val hasShadow: Boolean = true,
    val opacity: Float = 1.0f,
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val isLocked: Boolean = false,
    val zIndex: Int = 0
)

data class StickerPresetGroup(
    val categoryName: String,
    val items: List<String>
)

object DefaultOverlayLibrary {
    val EMOJI_GROUPS = listOf(
        StickerPresetGroup("TRENDING", listOf("✨", "❤️", "⚡", "🔥", "📸", "🎬", "📼", "🌸")),
        StickerPresetGroup("RETRO", listOf("📺", "📻", "📹", "📼", "🎙️", "🕹️", "📼", "📀")),
        StickerPresetGroup("VIBES", listOf("✨", "🌟", "💫", "💖", "😎", "🌈", "⚡", "🔮")),
        StickerPresetGroup("TEXT STICKERS", listOf("Good Vibes", "Retro Mood", "VINCAM", "90S KIDS", "FILM 35MM", "LIVE RECORDING"))
    )

    val FRAME_PRESETS = listOf(
        "None",
        "35mm Film Strip",
        "Kodak 400 Film",
        "Vintage Polaroid",
        "Retro Viewfinder",
        "Classic Date Stamp",
        "Cinema Widescreen Border",
        "Chunky Retro Frame",
        "CRT Scanlines"
    )

    val FONTS = listOf(
        "Retro Vintage",
        "Classic Serif",
        "Modern Sans",
        "Neon Handwritten",
        "Monospace Tech",
        "Nepali / Hindi Standard",
        "Bold Poster"
    )
}
