package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ColorMatrix
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaItem
import com.example.data.MediaStoreRepository
import com.example.data.PresetRepository
import com.example.data.VinCamDatabase
import com.example.model.CameraMode
import com.example.model.CameraPresetEntity
import com.example.model.DefaultOverlayLibrary
import com.example.model.FilterCategory
import com.example.model.FilterPreset
import com.example.model.OverlayItem
import com.example.model.OverlayType
import com.example.model.ProCameraSettings
import com.example.model.VideoConfig
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.model.VinCamThemeOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class VinCamUiState(
    val currentMode: CameraMode = CameraMode.PHOTO,
    val selectedFilter: FilterPreset = FilterPreset.NONE,
    val filterIntensity: Float = 1.0f,
    val selectedCategory: FilterCategory = FilterCategory.RETRO,
    val exposureEv: Float = 0.0f, // -2.0 to +2.0
    val temperatureOffset: Float = 0.0f, // -50.0 to +50.0
    val aspectRatio: String = "16:9",
    val timerSeconds: Int = 0, // 0, 3, 5, 10
    val countdownRemaining: Int = 0,
    val isCountingDown: Boolean = false,
    val flashMode: Int = 0, // 0: OFF, 1: AUTO, 2: ON, 3: TORCH
    val isFrontCamera: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val gridType: String = "3x3", // OFF, 3x3, GOLDEN, SQUARE
    val overlays: List<OverlayItem> = emptyList(),
    val selectedOverlayId: String? = null,
    val proSettings: ProCameraSettings = ProCameraSettings(),
    val videoConfig: VideoConfig = VideoConfig(),
    val isRecording: Boolean = false,
    val isRecordingPaused: Boolean = false,
    val recordingDurationSeconds: Long = 0L,
    val autoSaveToGallery: Boolean = true,
    val currentTheme: VinCamThemeOption = VinCamThemeOption.VINCAM_PASTEL,
    val shutterButtonStyle: String = "CLASSIC_GOLD", // CLASSIC_GOLD, VINTAGE_CHROME, PASTEL_ROSE, LEICA_RED, NEON_CYAN
    val isAeAfLocked: Boolean = false,
    val galleryItems: List<MediaItem> = emptyList(),
    val lastCapturedUri: Uri? = null,
    val pendingSaveItem: MediaItem? = null,
    val isDrawerOpen: Boolean = false,
    val activeDialogOrSheet: String? = null,
    val toastMessage: String? = null
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaStoreRepository = MediaStoreRepository(application)
    private val presetRepository = PresetRepository(
        VinCamDatabase.getDatabase(application).presetDao()
    )

    private val _uiState = MutableStateFlow(VinCamUiState())
    val uiState: StateFlow<VinCamUiState> = _uiState.asStateFlow()

    val savedUserPresets: StateFlow<List<CameraPresetEntity>> = presetRepository.allPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var recordingTimerJob: Job? = null
    private var countdownTimerJob: Job? = null

    init {
        loadGalleryItems()
    }

    fun setCameraMode(mode: CameraMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun selectFilter(preset: FilterPreset) {
        _uiState.update {
            it.copy(
                selectedFilter = preset,
                filterIntensity = preset.defaultIntensity
            )
        }
    }

    fun setFilterIntensity(intensity: Float) {
        _uiState.update { it.copy(filterIntensity = intensity.coerceIn(0f, 1f)) }
    }

    fun selectFilterCategory(category: FilterCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setExposureEv(ev: Float) {
        _uiState.update { it.copy(exposureEv = ev.coerceIn(-2.0f, 2.0f)) }
    }

    fun resetExposure() {
        _uiState.update { it.copy(exposureEv = 0.0f) }
    }

    fun setTemperatureOffset(temp: Float) {
        _uiState.update { it.copy(temperatureOffset = temp.coerceIn(-50.0f, 50.0f)) }
    }

    fun resetTemperature() {
        _uiState.update { it.copy(temperatureOffset = 0.0f) }
    }

    fun setAspectRatio(ratio: String) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun cycleAspectRatio() {
        _uiState.update {
            val next = when (it.aspectRatio) {
                "16:9" -> "4:3"
                "4:3" -> "1:1"
                "1:1" -> "9:16"
                "9:16" -> "2.39:1"
                "2.39:1" -> "POLAROID"
                else -> "16:9"
            }
            it.copy(aspectRatio = next)
        }
    }

    fun cycleShutterStyle() {
        _uiState.update {
            val next = when (it.shutterButtonStyle) {
                "CLASSIC_GOLD" -> "VINTAGE_CHROME"
                "VINTAGE_CHROME" -> "PASTEL_ROSE"
                "PASTEL_ROSE" -> "LEICA_RED"
                "LEICA_RED" -> "NEON_CYAN"
                else -> "CLASSIC_GOLD"
            }
            it.copy(shutterButtonStyle = next, toastMessage = "Shutter: ${next.replace('_', ' ')}")
        }
    }

    fun toggleAeAfLock() {
        _uiState.update {
            val nextLock = !it.isAeAfLocked
            it.copy(
                isAeAfLocked = nextLock,
                toastMessage = if (nextLock) "AE/AF LOCKED" else "AE/AF UNLOCKED"
            )
        }
    }

    fun cycleFlashMode() {
        _uiState.update {
            val next = (it.flashMode + 1) % 4
            it.copy(flashMode = next)
        }
    }

    fun cycleTimerDuration() {
        _uiState.update {
            val next = when (it.timerSeconds) {
                0 -> 3
                3 -> 5
                5 -> 10
                else -> 0
            }
            it.copy(timerSeconds = next)
        }
    }

    fun toggleCameraLens() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun setZoomRatio(zoom: Float) {
        _uiState.update { it.copy(zoomRatio = zoom.coerceIn(0.5f, 5.0f)) }
    }

    fun cycleGridType() {
        _uiState.update {
            val next = when (it.gridType) {
                "OFF" -> "3x3"
                "3x3" -> "GOLDEN"
                "GOLDEN" -> "SQUARE"
                else -> "OFF"
            }
            it.copy(gridType = next)
        }
    }

    // Overlays Management
    fun addTextOverlay(text: String, font: String = "Retro Vintage", colorHex: String = "#FFFFFF") {
        if (text.isBlank()) return
        val item = OverlayItem(
            id = UUID.randomUUID().toString(),
            type = OverlayType.TEXT,
            textContent = text,
            fontStyle = font,
            colorHex = colorHex,
            xRatio = 0.5f,
            yRatio = 0.5f
        )
        _uiState.update {
            it.copy(
                overlays = it.overlays + item,
                selectedOverlayId = item.id
            )
        }
    }

    fun addStickerOverlay(emojiOrAsset: String, type: OverlayType = OverlayType.EMOJI) {
        val item = OverlayItem(
            id = UUID.randomUUID().toString(),
            type = type,
            assetNameOrEmoji = emojiOrAsset,
            xRatio = 0.5f,
            yRatio = 0.4f
        )
        _uiState.update {
            it.copy(
                overlays = it.overlays + item,
                selectedOverlayId = item.id
            )
        }
    }

    fun setFrameOverlay(frameName: String) {
        val filtered = _uiState.value.overlays.filterNot { it.type == OverlayType.FRAME }
        if (frameName != "None") {
            val frameItem = OverlayItem(
                id = UUID.randomUUID().toString(),
                type = OverlayType.FRAME,
                assetNameOrEmoji = frameName,
                xRatio = 0.5f,
                yRatio = 0.5f,
                scale = 1.0f
            )
            _uiState.update { it.copy(overlays = filtered + frameItem) }
        } else {
            _uiState.update { it.copy(overlays = filtered) }
        }
    }

    fun updateOverlayPosition(id: String, xRatio: Float, yRatio: Float) {
        _uiState.update { state ->
            val updated = state.overlays.map { item ->
                if (item.id == id && !item.isLocked) {
                    item.copy(xRatio = xRatio.coerceIn(0.05f, 0.95f), yRatio = yRatio.coerceIn(0.05f, 0.95f))
                } else item
            }
            state.copy(overlays = updated)
        }
    }

    fun updateOverlayTransform(id: String, scaleDelta: Float, rotationDelta: Float) {
        _uiState.update { state ->
            val updated = state.overlays.map { item ->
                if (item.id == id && !item.isLocked) {
                    val newScale = (item.scale * scaleDelta).coerceIn(0.3f, 4.0f)
                    val newRot = (item.rotation + rotationDelta) % 360f
                    item.copy(scale = newScale, rotation = newRot)
                } else item
            }
            state.copy(overlays = updated)
        }
    }

    fun selectOverlay(id: String?) {
        _uiState.update { it.copy(selectedOverlayId = id) }
    }

    fun deleteOverlay(id: String) {
        _uiState.update { state ->
            state.copy(
                overlays = state.overlays.filterNot { it.id == id },
                selectedOverlayId = if (state.selectedOverlayId == id) null else state.selectedOverlayId
            )
        }
    }

    fun duplicateOverlay(id: String) {
        val original = _uiState.value.overlays.find { it.id == id } ?: return
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            xRatio = (original.xRatio + 0.05f).coerceAtMost(0.9f),
            yRatio = (original.yRatio + 0.05f).coerceAtMost(0.9f)
        )
        _uiState.update { it.copy(overlays = it.overlays + copy, selectedOverlayId = copy.id) }
    }

    fun toggleOverlayLock(id: String) {
        _uiState.update { state ->
            val updated = state.overlays.map {
                if (it.id == id) it.copy(isLocked = !it.isLocked) else it
            }
            state.copy(overlays = updated)
        }
    }

    fun setOverlayOpacity(id: String, opacity: Float) {
        _uiState.update { state ->
            val updated = state.overlays.map {
                if (it.id == id) it.copy(opacity = opacity.coerceIn(0.1f, 1.0f)) else it
            }
            state.copy(overlays = updated)
        }
    }

    // Pro Settings Updates
    fun updateProSettings(transform: (ProCameraSettings) -> ProCameraSettings) {
        _uiState.update { it.copy(proSettings = transform(it.proSettings)) }
    }

    // Video Config Updates
    fun updateVideoConfig(transform: (VideoConfig) -> VideoConfig) {
        _uiState.update { it.copy(videoConfig = transform(it.videoConfig)) }
    }

    // Recording Controls
    fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        _uiState.update { it.copy(isRecording = true, isRecordingPaused = false, recordingDurationSeconds = 0L) }
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isRecordingPaused) {
                    _uiState.update { it.copy(recordingDurationSeconds = it.recordingDurationSeconds + 1) }
                }
            }
        }
    }

    fun pauseRecording() {
        _uiState.update { it.copy(isRecordingPaused = true) }
    }

    fun resumeRecording() {
        _uiState.update { it.copy(isRecordingPaused = false) }
    }

    fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        _uiState.update { it.copy(isRecording = false, isRecordingPaused = false, recordingDurationSeconds = 0L) }
    }

    // Save Photo & Video Actions
    fun processAndSaveCapturedPhoto(bitmap: Bitmap, onSaved: (Uri?) -> Unit) {
        viewModelScope.launch {
            val savedUri = mediaStoreRepository.savePhotoToMediaStore(bitmap)
            if (savedUri != null) {
                _uiState.update { it.copy(lastCapturedUri = savedUri, toastMessage = "Photo saved to Gallery") }
                loadGalleryItems()
            }
            onSaved(savedUri)
        }
    }

    fun processAndSaveCapturedVideo(videoFile: File, onSaved: (Uri?) -> Unit) {
        viewModelScope.launch {
            val savedUri = mediaStoreRepository.saveVideoFileToMediaStore(videoFile)
            if (savedUri != null) {
                _uiState.update { it.copy(lastCapturedUri = savedUri, toastMessage = "Video saved to Gallery") }
                loadGalleryItems()
            }
            onSaved(savedUri)
        }
    }

    fun toggleAutoSave() {
        _uiState.update { it.copy(autoSaveToGallery = !it.autoSaveToGallery) }
    }

    fun setTheme(theme: VinCamThemeOption) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun loadGalleryItems() {
        viewModelScope.launch {
            val items = mediaStoreRepository.getRecentMediaItems()
            _uiState.update {
                it.copy(
                    galleryItems = items,
                    lastCapturedUri = items.firstOrNull()?.uri
                )
            }
        }
    }

    // Saved Presets DB
    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            val state = uiState.value
            val presetEntity = CameraPresetEntity(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Preset ${System.currentTimeMillis() % 1000}" },
                filterId = state.selectedFilter.id,
                filterIntensity = state.filterIntensity,
                exposure = state.exposureEv,
                temperature = state.temperatureOffset,
                tint = state.proSettings.wbTint.toFloat(),
                contrast = state.selectedFilter.contrastOffset,
                saturation = state.selectedFilter.saturationOffset,
                grain = state.selectedFilter.grainLevel,
                vignette = state.selectedFilter.vignetteLevel,
                frameName = state.overlays.find { it.type == OverlayType.FRAME }?.assetNameOrEmoji ?: "None",
                textOverlay = state.overlays.find { it.type == OverlayType.TEXT }?.textContent ?: ""
            )
            presetRepository.savePreset(presetEntity)
            showToast("Preset '$name' saved!")
        }
    }

    fun applySavedPreset(presetEntity: CameraPresetEntity) {
        val filter = FilterPreset.ALL_PRESETS.find { it.id == presetEntity.filterId } ?: FilterPreset.NONE
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filterIntensity = presetEntity.filterIntensity,
                exposureEv = presetEntity.exposure,
                temperatureOffset = presetEntity.temperature
            )
        }
        if (presetEntity.frameName.isNotBlank() && presetEntity.frameName != "None") {
            setFrameOverlay(presetEntity.frameName)
        }
        showToast("Preset '${presetEntity.name}' applied")
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            presetRepository.deletePreset(id)
            showToast("Preset deleted")
        }
    }

    fun togglePresetFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            presetRepository.toggleFavorite(id, isFavorite)
        }
    }

    // UI Dialog & Drawer states
    fun openDrawer() { _uiState.update { it.copy(isDrawerOpen = true) } }
    fun closeDrawer() { _uiState.update { it.copy(isDrawerOpen = false) } }

    fun openDialogOrSheet(sheetName: String?) {
        _uiState.update { it.copy(activeDialogOrSheet = sheetName, isDrawerOpen = false) }
    }

    fun closeDialogOrSheet() {
        _uiState.update { it.copy(activeDialogOrSheet = null) }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
