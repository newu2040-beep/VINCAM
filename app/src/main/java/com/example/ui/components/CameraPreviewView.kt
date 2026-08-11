package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.CameraMode
import com.example.model.FilterPreset
import com.example.model.OverlayItem
import com.example.model.VideoConfig
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.viewmodel.VinCamUiState
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraPreviewContainer(
    uiState: VinCamUiState,
    onTapFocus: (Offset) -> Unit,
    onZoomChange: (Float) -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit,
    onVideoCaptureFinished: (File) -> Unit,
    setCaptureController: (CameraCaptureController) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    var tapFocusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusRingScale = remember { Animatable(1.5f) }
    val focusRingAlpha = remember { Animatable(1.0f) }
    val coroutineScope = rememberCoroutineScope()
    
    val mediaActionSound = remember { android.media.MediaActionSound() }
    DisposableEffect(Unit) {
        mediaActionSound.load(android.media.MediaActionSound.SHUTTER_CLICK)
        mediaActionSound.load(android.media.MediaActionSound.START_VIDEO_RECORDING)
        mediaActionSound.load(android.media.MediaActionSound.STOP_VIDEO_RECORDING)
        onDispose { mediaActionSound.release() }
    }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Initialize Camera Provider & Bind Lifecycle cleanly
    LaunchedEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(cameraProvider, uiState.isFrontCamera, uiState.videoConfig.resolution, uiState.currentMode, previewViewRef) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val pView = previewViewRef ?: return@LaunchedEffect

        // Do not unbind while recording is actively in progress
        if (activeRecording != null) return@LaunchedEffect

        val selector = if (uiState.isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = pView.surfaceProvider
        }

        val imgCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(
                when (uiState.flashMode) {
                    1 -> ImageCapture.FLASH_MODE_AUTO
                    2, 3 -> ImageCapture.FLASH_MODE_ON
                    else -> ImageCapture.FLASH_MODE_OFF
                }
            )
            .build()
        imageCapture = imgCapture

        val preferredQuality = when (uiState.videoConfig.resolution) {
            VideoResolution.RES_4K -> Quality.UHD
            VideoResolution.RES_1440P -> Quality.FHD
            VideoResolution.RES_1080P -> Quality.FHD
            VideoResolution.RES_720P -> Quality.HD
        }

        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(preferredQuality, Quality.FHD, Quality.HD, Quality.SD, Quality.LOWEST),
            FallbackStrategy.lowerQualityOrHigherThan(Quality.LOWEST)
        )

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        val vidCapture = VideoCapture.withOutput(recorder)
        videoCapture = vidCapture

        try {
            provider.unbindAll()
            val boundCamera = if (uiState.currentMode == CameraMode.VIDEO) {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, vidCapture)
            } else {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imgCapture)
            }
            camera = boundCamera
        } catch (e: Exception) {
            Log.e("VinCam", "Camera binding failed: ${e.message}", e)
            Toast.makeText(context, "Error: Device does not support this camera resolution or mode.", Toast.LENGTH_LONG).show()
        }
    }

    // Set up Capture Controller for ViewModel/UI triggers
    DisposableEffect(imageCapture, videoCapture, camera) {
        val controller = object : CameraCaptureController {
            override fun takePhoto() {
                val capture = imageCapture ?: return
                mediaActionSound.play(android.media.MediaActionSound.SHUTTER_CLICK)
                capture.takePicture(
                    Executors.newSingleThreadExecutor(),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val originalBitmap = image.toBitmap()
                            val matrix = Matrix()
                            matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
                            if (uiState.isFrontCamera) {
                                matrix.postScale(-1f, 1f)
                            }
                            val rotatedBitmap = Bitmap.createBitmap(
                                originalBitmap, 0, 0,
                                originalBitmap.width, originalBitmap.height,
                                matrix, true
                            )
                            image.close()

                            // Process bitmap with Filter ColorMatrix
                            val processedBitmap = applyFilterToBitmap(
                                source = rotatedBitmap,
                                filter = uiState.selectedFilter,
                                intensity = uiState.filterIntensity,
                                exposureEv = uiState.exposureEv,
                                tempOffset = uiState.temperatureOffset,
                                overlays = uiState.overlays
                            )
                            onPhotoCaptured(processedBitmap)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("VinCam", "Photo capture failed: ${exception.message}", exception)
                        }
                    }
                )
            }

            override fun startVideoRecording() {
                val vCapture = videoCapture ?: return
                val videoFile = File(context.cacheDir, "vincam_record_${System.currentTimeMillis()}.mp4")
                val outputOptions = FileOutputOptions.Builder(videoFile).build()

                try {
                    var pendingRecording = vCapture.output.prepareRecording(context, outputOptions)
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        pendingRecording = pendingRecording.withAudioEnabled()
                    }

                    mediaActionSound.play(android.media.MediaActionSound.START_VIDEO_RECORDING)
                    activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
                        when (event) {
                            is VideoRecordEvent.Finalize -> {
                                if (!event.hasError() || videoFile.exists() && videoFile.length() > 0) {
                                    onVideoCaptureFinished(videoFile)
                                } else {
                                    Log.e("VinCam", "Video record finalize error: ${event.error}")
                                }
                                activeRecording = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VinCam", "Failed to start recording: ${e.message}", e)
                }
            }

            override fun pauseVideoRecording() {
                activeRecording?.pause()
            }

            override fun resumeVideoRecording() {
                activeRecording?.resume()
            }

            override fun stopVideoRecording() {
                mediaActionSound.play(android.media.MediaActionSound.STOP_VIDEO_RECORDING)
                activeRecording?.stop()
                activeRecording = null
            }
        }
        setCaptureController(controller)
        onDispose { }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(uiState.zoomRatio) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) {
                        onZoomChange(uiState.zoomRatio * zoom)
                    }
                }
            }
    ) {
        val filterMatrix = remember(uiState.selectedFilter, uiState.filterIntensity, uiState.exposureEv, uiState.temperatureOffset) {
            val baseMatrix = uiState.selectedFilter.getAdjustedColorMatrix(uiState.filterIntensity)
            val arr = baseMatrix.values.clone()

            // Apply exposure EV shift
            val evShift = (uiState.exposureEv * 25f)
            arr[4] += evShift
            arr[9] += evShift
            arr[14] += evShift

            // Apply temperature shift (warm -> R+, B-, cool -> R-, B+)
            val tempShift = uiState.temperatureOffset * 0.4f
            arr[4] += tempShift
            arr[14] -= tempShift

            android.graphics.ColorMatrix(arr)
        }

        // CameraX PreviewView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER

                    val scaleGestureDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            onZoomChange(uiState.zoomRatio * detector.scaleFactor)
                            return true
                        }
                    })

                    val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            val point = Offset(e.x, e.y)
                            onTapFocus(point)
                            tapFocusPoint = point

                            // Focus metering action
                            val meteringPoint = SurfaceOrientedMeteringPointFactory(width.toFloat(), height.toFloat())
                                .createPoint(e.x, e.y)
                            val action = FocusMeteringAction.Builder(meteringPoint).build()
                            camera?.cameraControl?.startFocusAndMetering(action)

                            coroutineScope.launch {
                                focusRingScale.snapTo(1.5f)
                                focusRingAlpha.snapTo(1.0f)
                                focusRingScale.animateTo(1.0f, animationSpec = tween(250))
                                focusRingAlpha.animateTo(0.0f, animationSpec = tween(500, delayMillis = 300))
                            }
                            return true
                        }
                    })

                    setOnTouchListener { _, event ->
                        scaleGestureDetector.onTouchEvent(event)
                        gestureDetector.onTouchEvent(event)
                        true
                    }
                    previewViewRef = this
                }
            },
            update = { previewView ->
                // Apply color filter to the view layer natively
                val paint = android.graphics.Paint()
                paint.colorFilter = android.graphics.ColorMatrixColorFilter(filterMatrix)
                previewView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, paint)
                
                // Update non-destructive controls (Zoom and Torch) safely without calling unbindAll
                try {
                    val zoomState = camera?.cameraInfo?.zoomState?.value
                    val minZ = zoomState?.minZoomRatio ?: 1.0f
                    val maxZ = zoomState?.maxZoomRatio ?: 5.0f
                    val targetZoom = uiState.zoomRatio.coerceIn(minZ, maxZ)
                    
                    camera?.cameraControl?.setZoomRatio(targetZoom)
                    if (uiState.flashMode == 3) {
                        camera?.cameraControl?.enableTorch(true)
                    } else {
                        camera?.cameraControl?.enableTorch(false)
                    }
                } catch (e: Exception) {
                    Log.e("VinCam", "Failed updating camera controls: ${e.message}")
                }
            }
        )

        // Tap-to-focus ring indicator
        tapFocusPoint?.let { point ->
            if (focusRingAlpha.value > 0f) {
                val strokeColor = Color(0xFFFF6321).copy(alpha = focusRingAlpha.value)
                Canvas(
                    modifier = Modifier
                        .size(64.dp)
                        .offset {
                            IntOffset(
                                (point.x - 32.dp.toPx()).toInt(),
                                (point.y - 32.dp.toPx()).toInt()
                            )
                        }
                ) {
                    drawCircle(
                        color = strokeColor,
                        radius = (size.minDimension / 2) * focusRingScale.value,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}

interface CameraCaptureController {
    fun takePhoto()
    fun startVideoRecording()
    fun pauseVideoRecording()
    fun resumeVideoRecording()
    fun stopVideoRecording()
}

fun applyFilterToBitmap(
    source: Bitmap,
    filter: FilterPreset,
    intensity: Float,
    exposureEv: Float,
    tempOffset: Float,
    overlays: List<OverlayItem>
): Bitmap {
    val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val colorMatrix = filter.getAdjustedColorMatrix(intensity)
    val arr = colorMatrix.values.clone()

    val evShift = (exposureEv * 25f)
    arr[4] += evShift
    arr[9] += evShift
    arr[14] += evShift

    val tempShift = tempOffset * 0.4f
    arr[4] += tempShift
    arr[14] -= tempShift

    paint.colorFilter = ColorMatrixColorFilter(arr)
    canvas.drawBitmap(source, 0f, 0f, paint)

    // Render Text and Overlays onto captured bitmap
    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = source.width * 0.05f
        color = android.graphics.Color.WHITE
    }

    overlays.forEach { item ->
        val x = source.width * item.xRatio
        val y = source.height * item.yRatio
        if (item.textContent.isNotBlank()) {
            canvas.drawText(item.textContent, x, y, overlayPaint)
        } else if (item.assetNameOrEmoji.isNotBlank()) {
            canvas.drawText(item.assetNameOrEmoji, x, y, overlayPaint)
        }
    }

    return result
}
