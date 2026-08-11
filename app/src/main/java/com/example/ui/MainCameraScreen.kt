package com.example.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.ui.components.AboutBottomSheet
import com.example.ui.components.BottomBarControls
import com.example.ui.components.CameraCaptureController
import com.example.ui.components.CameraPreviewContainer
import com.example.ui.components.ExposureTempOverlaySliders
import com.example.ui.components.FilterPickerBottomSheet
import com.example.ui.components.GalleryBottomSheet
import com.example.ui.components.HamburgerDrawerContent
import com.example.ui.components.HelpManualBottomSheet
import com.example.ui.components.OverlayCanvas
import com.example.ui.components.OverlayEditorBottomSheet
import com.example.ui.components.PermissionOnboardingScreen
import com.example.ui.components.PermissionsBottomSheet
import com.example.ui.components.PresetManagerBottomSheet
import com.example.ui.components.PrivacyPolicyBottomSheet
import com.example.ui.components.ProModeControlsOverlay
import com.example.ui.components.StorageBottomSheet
import com.example.ui.components.ThemeSelectorBottomSheet
import com.example.ui.components.TopBarControls
import com.example.ui.components.VideoSettingsBottomSheet
import com.example.ui.components.ZoomLensBar
import com.example.viewmodel.CameraViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainCameraScreen(
    viewModel: CameraViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedPresets by viewModel.savedUserPresets.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var captureController by remember { mutableStateOf<CameraCaptureController?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Permission check using Accompanist
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    // Sync ViewModel Toast with Android Toast or Banner
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (!permissionsState.allPermissionsGranted) {
        PermissionOnboardingScreen(
            onRequestPermissions = {
                permissionsState.launchMultiplePermissionRequest()
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            HamburgerDrawerContent(
                uiState = uiState,
                onNavigate = { sheetName ->
                    coroutineScope.launch { drawerState.close() }
                    viewModel.openDialogOrSheet(sheetName)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Black
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Live CameraX Preview Engine
                CameraPreviewContainer(
                    uiState = uiState,
                    onTapFocus = { focusOffset -> },
                    onZoomChange = { zoom -> viewModel.setZoomRatio(zoom) },
                    onPhotoCaptured = { bitmap ->
                        viewModel.processAndSaveCapturedPhoto(bitmap) { uri -> }
                    },
                    onVideoCaptureFinished = { videoFile ->
                        viewModel.processAndSaveCapturedVideo(videoFile) { uri -> }
                    },
                    setCaptureController = { controller -> captureController = controller }
                )

                // Interactive Overlays Canvas Layer
                OverlayCanvas(
                    uiState = uiState,
                    onSelectOverlay = { id -> viewModel.selectOverlay(id) },
                    onUpdatePosition = { id, x, y -> viewModel.updateOverlayPosition(id, x, y) },
                    onUpdateTransform = { id, scale, rot -> viewModel.updateOverlayTransform(id, scale, rot) },
                    onDeleteOverlay = { id -> viewModel.deleteOverlay(id) },
                    onDuplicateOverlay = { id -> viewModel.duplicateOverlay(id) },
                    onToggleLock = { id -> viewModel.toggleOverlayLock(id) },
                    onSetOpacity = { id, op -> viewModel.setOverlayOpacity(id, op) }
                )

                // Top Bar Controls Overlay
                TopBarControls(
                    uiState = uiState,
                    onOpenMenu = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onCycleFlash = { viewModel.cycleFlashMode() },
                    onCycleTimer = { viewModel.cycleTimerDuration() },
                    onCycleRatio = { viewModel.setAspectRatio(if (uiState.aspectRatio == "16:9") "4:3" else if (uiState.aspectRatio == "4:3") "1:1" else "16:9") },
                    onSwitchCamera = { viewModel.toggleCameraLens() },
                    onOpenSettings = { viewModel.openDialogOrSheet("VIDEO_CONFIG") },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Center / Bottom Controls Column
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pro Mode Specific Workspace Controls
                    AnimatedVisibility(visible = uiState.currentMode == CameraMode.PRO) {
                        ProModeControlsOverlay(
                            uiState = uiState,
                            onUpdateProSettings = { transform -> viewModel.updateProSettings(transform) }
                        )
                    }

                    // Exposure & Temperature Sliders
                    ExposureTempOverlaySliders(
                        uiState = uiState,
                        onExposureChanged = { ev -> viewModel.setExposureEv(ev) },
                        onResetExposure = { viewModel.resetExposure() },
                        onTemperatureChanged = { temp -> viewModel.setTemperatureOffset(temp) },
                        onResetTemperature = { viewModel.resetTemperature() }
                    )

                    // Zoom Lens Selector (0.5x, 1x, 2x, 3x, 5x)
                    ZoomLensBar(
                        uiState = uiState,
                        onZoomSelected = { zoom -> viewModel.setZoomRatio(zoom) }
                    )

                    // Bottom Bar Controls (Gallery, Retro Shutter, Mode Selector)
                    BottomBarControls(
                        uiState = uiState,
                        onModeSelected = { mode -> viewModel.setCameraMode(mode) },
                        onShutterClicked = {
                            when (uiState.currentMode) {
                                CameraMode.PHOTO, CameraMode.PRO -> {
                                    captureController?.takePhoto()
                                }
                                CameraMode.VIDEO -> {
                                    if (uiState.isRecording) {
                                        captureController?.stopVideoRecording()
                                        viewModel.stopRecordingTimer()
                                    } else {
                                        captureController?.startVideoRecording()
                                        viewModel.startRecordingTimer()
                                    }
                                }
                            }
                        },
                        onPauseResumeVideo = {
                            if (uiState.isRecordingPaused) {
                                captureController?.resumeVideoRecording()
                                viewModel.resumeRecording()
                            } else {
                                captureController?.pauseVideoRecording()
                                viewModel.pauseRecording()
                            }
                        },
                        onOpenGallery = { viewModel.openDialogOrSheet("GALLERY") },
                        onOpenLut = { viewModel.openDialogOrSheet("LUT") }
                    )
                }

                // Modal Bottom Sheets
                when (uiState.activeDialogOrSheet) {
                    "PRO_SETTINGS" -> {
                        LaunchedEffect(Unit) {
                            viewModel.setCameraMode(CameraMode.PRO)
                            viewModel.closeDialogOrSheet()
                        }
                    }
                    "LUT" -> {
                        FilterPickerBottomSheet(
                            uiState = uiState,
                            onCategorySelected = { cat -> viewModel.selectFilterCategory(cat) },
                            onFilterSelected = { preset -> viewModel.selectFilter(preset) },
                            onIntensityChanged = { intensity -> viewModel.setFilterIntensity(intensity) },
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "OVERLAYS" -> {
                        OverlayEditorBottomSheet(
                            uiState = uiState,
                            onAddText = { text, font -> viewModel.addTextOverlay(text, font) },
                            onAddSticker = { sticker -> viewModel.addStickerOverlay(sticker) },
                            onSelectFrame = { frame -> viewModel.setFrameOverlay(frame) },
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "PRESETS" -> {
                        PresetManagerBottomSheet(
                            uiState = uiState,
                            savedPresets = savedPresets,
                            onSaveCurrentAsPreset = { name -> viewModel.saveCurrentAsPreset(name) },
                            onApplyPreset = { preset -> viewModel.applySavedPreset(preset) },
                            onDeletePreset = { id -> viewModel.deletePreset(id) },
                            onToggleFavorite = { id, isFav -> viewModel.togglePresetFavorite(id, isFav) },
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "VIDEO_CONFIG" -> {
                        VideoSettingsBottomSheet(
                            uiState = uiState,
                            onUpdateVideoConfig = { transform -> viewModel.updateVideoConfig(transform) },
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "THEMES" -> {
                        ThemeSelectorBottomSheet(
                            uiState = uiState,
                            onSelectTheme = { theme -> viewModel.setTheme(theme) },
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "GALLERY" -> {
                        GalleryBottomSheet(
                            uiState = uiState,
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "STORAGE" -> {
                        StorageBottomSheet(
                            uiState = uiState,
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "PERMISSIONS" -> {
                        PermissionsBottomSheet(
                            uiState = uiState,
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "PRIVACY" -> {
                        PrivacyPolicyBottomSheet(
                            uiState = uiState,
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "HELP" -> {
                        HelpManualBottomSheet(
                            uiState = uiState,
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                    "ABOUT" -> {
                        AboutBottomSheet(
                            uiState = uiState,
                            onDismiss = { viewModel.closeDialogOrSheet() }
                        )
                    }
                }
            }
        }
    }
}
