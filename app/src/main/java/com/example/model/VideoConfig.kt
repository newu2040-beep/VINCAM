package com.example.model

enum class VideoResolution(val label: String, val width: Int, val height: Int) {
    RES_720P("720p", 1280, 720),
    RES_1080P("1080p", 1920, 1080),
    RES_1440P("1440p", 2560, 1440),
    RES_4K("4K", 3840, 2160)
}

enum class VideoFps(val fpsValue: Int) {
    FPS_24(24),
    FPS_25(25),
    FPS_30(30),
    FPS_60(60)
}

enum class BitratePreset(val label: String, val defaultMbps: Int) {
    AUTO("Auto", 12),
    LOW("Low", 6),
    MEDIUM("Medium", 12),
    HIGH("High", 24),
    MAXIMUM("Maximum", 48),
    CUSTOM("Custom", 18)
}

enum class VideoCodec(val label: String) {
    H264("H.264 (AVC)"),
    H265("H.265 (HEVC)")
}

data class VideoConfig(
    val resolution: VideoResolution = VideoResolution.RES_1080P,
    val fps: VideoFps = VideoFps.FPS_30,
    val codec: VideoCodec = VideoCodec.H264,
    val bitratePreset: BitratePreset = BitratePreset.AUTO,
    val customBitrateMbps: Int = 12,
    val isStabilizationEnabled: Boolean = true,
    val supportedResolutions: List<VideoResolution> = VideoResolution.entries.toList(),
    val supportedFps: List<VideoFps> = VideoFps.entries.toList()
) {
    fun getEffectiveBitrateBps(): Int {
        val mbps = when (bitratePreset) {
            BitratePreset.AUTO -> when (resolution) {
                VideoResolution.RES_720P -> 6
                VideoResolution.RES_1080P -> 12
                VideoResolution.RES_1440P -> 24
                VideoResolution.RES_4K -> 42
            }
            BitratePreset.LOW -> 6
            BitratePreset.MEDIUM -> 12
            BitratePreset.HIGH -> 24
            BitratePreset.MAXIMUM -> 48
            BitratePreset.CUSTOM -> customBitrateMbps.coerceIn(2, 80)
        }
        return mbps * 1_000_000
    }
}
