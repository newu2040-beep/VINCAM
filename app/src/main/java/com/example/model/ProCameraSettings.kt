package com.example.model

data class ProCameraSettings(
    val iso: String = "AUTO", // AUTO, 100, 200, 400, 800, 1600, 3200, 6400
    val shutterSpeed: String = "AUTO", // AUTO, 1/1000s, 1/500s, 1/250s, 1/125s, 1/60s, 1/30s, 1/15s
    val focusMode: String = "AF", // AF (Auto Focus), MF (Manual Focus), LOCK
    val focusDistance: Float = 0.5f, // 0.0 (Infinity) to 1.0 (Macro)
    val wbMode: String = "AUTO", // AUTO, CLOUDY, DAYLIGHT, FLUORESCENT, INCANDESCENT, CUSTOM
    val wbTemperatureK: Int = 5500, // 2500K to 7500K
    val wbTint: Int = 0, // -50 to +50
    val exposureCompensation: Float = 0.0f, // -2.0 to +2.0 EV
    val isFocusPeakingEnabled: Boolean = false,
    val isHistogramEnabled: Boolean = false,
    val isZebraStripesEnabled: Boolean = false,
    val lensSelection: String = "1x", // 0.5x, 1x, 2x, 3x, 5x
    val isRawEnabled: Boolean = false,
    val isHdrEnabled: Boolean = false,
    val isStabilizationEnabled: Boolean = true,
    val noiseReductionMode: String = "High Quality" // Off, Fast, High Quality
)
