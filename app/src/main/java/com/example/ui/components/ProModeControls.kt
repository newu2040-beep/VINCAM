package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.ProCameraSettings
import com.example.viewmodel.VinCamUiState

@Composable
fun ProModeControlsOverlay(
    uiState: VinCamUiState,
    onUpdateProSettings: ((ProCameraSettings) -> ProCameraSettings) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Pro Mode replaced by CC (Color Correction) Mode
    CcModeControlsOverlay(
        uiState = uiState,
        onSetExposureEv = {},
        onSetContrast = {},
        onSetSaturation = {},
        onSetTemperature = {},
        onSetTint = {},
        onSetGrain = {},
        onResetAllCc = {},
        modifier = modifier
    )
}


