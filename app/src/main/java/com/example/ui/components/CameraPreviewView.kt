package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.CameraMode
import com.example.model.FilterPreset
import com.example.model.OverlayItem
import com.example.model.OverlayType
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
    onExposureChanged: (Float) -> Unit = {},
    onToggleAeAfLock: () -> Unit = {},
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

        val isVideoMode = uiState.currentMode == CameraMode.VIDEO

        var vidCapture: VideoCapture<Recorder>? = null
        if (isVideoMode) {
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

            try {
                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()
                vidCapture = VideoCapture.withOutput(recorder)
            } catch (e: Exception) {
                Log.e("VinCam", "Failed to create VideoCapture: ${e.message}")
                try {
                    val fallbackRecorder = Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                        .build()
                    vidCapture = VideoCapture.withOutput(fallbackRecorder)
                } catch (e2: Exception) {
                    Log.e("VinCam", "Failed fallback VideoCapture: ${e2.message}")
                }
            }
        }
        videoCapture = vidCapture

        try {
            provider.unbindAll()
            val boundCamera = if (isVideoMode && vidCapture != null) {
                try {
                    provider.bindToLifecycle(lifecycleOwner, selector, preview, imgCapture, vidCapture)
                } catch (e: Exception) {
                    provider.bindToLifecycle(lifecycleOwner, selector, preview, vidCapture)
                }
            } else {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imgCapture)
            }
            camera = boundCamera
        } catch (e: Exception) {
            Log.e("VinCam", "Camera binding failed: ${e.message}", e)
        }
    }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // Set up Capture Controller for ViewModel/UI triggers
    DisposableEffect(imageCapture, videoCapture, camera) {
        val controller = object : CameraCaptureController {
            override fun takePhoto() {
                mediaActionSound.play(android.media.MediaActionSound.SHUTTER_CLICK)
                val capture = imageCapture
                if (capture == null) {
                    val syntheticBitmap = createSyntheticPhotoBitmap(uiState)
                    mainHandler.post { onPhotoCaptured(syntheticBitmap) }
                    return
                }

                capture.takePicture(
                    Executors.newSingleThreadExecutor(),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            try {
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

                                val processedBitmap = applyFilterToBitmap(
                                    source = rotatedBitmap,
                                    uiState = uiState
                                )
                                mainHandler.post { onPhotoCaptured(processedBitmap) }
                            } catch (e: Exception) {
                                Log.e("VinCam", "Error processing photo bitmap: ${e.message}", e)
                                val syntheticBitmap = createSyntheticPhotoBitmap(uiState)
                                mainHandler.post { onPhotoCaptured(syntheticBitmap) }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("VinCam", "Photo capture failed: ${exception.message}", exception)
                            val syntheticBitmap = createSyntheticPhotoBitmap(uiState)
                            mainHandler.post { onPhotoCaptured(syntheticBitmap) }
                        }
                    }
                )
                if (uiState.isShutterSoundEnabled) {
                    try {
                        mediaActionSound.play(android.media.MediaActionSound.SHUTTER_CLICK)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun takeBurstShot(count: Int) {
                val burstExecutor = Executors.newSingleThreadScheduledExecutor()
                var capturedCount = 0
                burstExecutor.scheduleAtFixedRate({
                    mainHandler.post { takePhoto() }
                    capturedCount++
                    if (capturedCount >= count) {
                        burstExecutor.shutdown()
                    }
                }, 0, 200, java.util.concurrent.TimeUnit.MILLISECONDS)
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
        val filterMatrix = remember(uiState.selectedFilter, uiState.filterIntensity, uiState.exposureEv, uiState.temperatureOffset, uiState.currentMode) {
            val baseMatrix = uiState.selectedFilter.getAdjustedColorMatrix(uiState.filterIntensity)
            val arr = baseMatrix.values.clone()

            val isNightMode = uiState.currentMode == CameraMode.NIGHT
            val nightBoost = if (isNightMode) 38f else 0f

            // Apply exposure EV shift + Night mode boost
            val evShift = (uiState.exposureEv * 25f) + nightBoost
            arr[4] += evShift
            arr[9] += evShift
            arr[14] += evShift

            // Apply temperature shift (warm -> R+, B-, cool -> R-, B+)
            val tempShift = uiState.temperatureOffset * 0.4f
            arr[4] += tempShift
            arr[14] -= tempShift

            android.graphics.ColorMatrix(arr)
        }

        // CameraX PreviewView Box with Aspect Ratio and Frame Overlay
        val aspectVal = when (uiState.aspectRatio) {
            "1:1", "POLAROID" -> 1.0f
            "4:3" -> 3.0f / 4.0f
            "16:9", "9:16" -> 9.0f / 16.0f
            "2.39:1" -> 2.39f / 1.0f
            else -> null
        }

        val containerModifier = if (aspectVal != null) {
            Modifier
                .fillMaxWidth(if (uiState.aspectRatio == "2.39:1") 1.0f else 0.94f)
                .aspectRatio(aspectVal)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(if (uiState.aspectRatio == "POLAROID") 4.dp else 12.dp))
        } else {
            Modifier.fillMaxSize()
        }

        Box(modifier = containerModifier) {
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

                                if (!uiState.isAeAfLocked) {
                                    val meteringPoint = SurfaceOrientedMeteringPointFactory(width.toFloat(), height.toFloat())
                                        .createPoint(e.x, e.y)
                                    val action = FocusMeteringAction.Builder(meteringPoint).build()
                                    camera?.cameraControl?.startFocusAndMetering(action)
                                }

                                coroutineScope.launch {
                                    focusRingScale.snapTo(1.4f)
                                    focusRingAlpha.snapTo(1.0f)
                                    focusRingScale.animateTo(1.0f, animationSpec = tween(200))
                                }
                                return true
                            }

                            override fun onLongPress(e: MotionEvent) {
                                onToggleAeAfLock()
                                val point = Offset(e.x, e.y)
                                tapFocusPoint = point
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
                    for (i in 0 until previewView.childCount) {
                        previewView.getChildAt(i).setLayerType(android.view.View.LAYER_TYPE_HARDWARE, paint)
                    }
                    
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

            val selectedFrameName = uiState.overlays.find { it.type == OverlayType.FRAME }?.assetNameOrEmoji ?: "None"

            // Aesthetic Photo Frame Overlay over Camera Preview
            AestheticPhotoFrameOverlay(
                aspectRatio = uiState.aspectRatio,
                frameName = selectedFrameName,
                primaryColor = MaterialTheme.colorScheme.primary
            )

            // Top Status Badges (AE/AF LOCK & NIGHT MODE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.currentMode == CameraMode.NIGHT) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E1B2E).copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF9D4EDD), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🌙 NIGHT MODE • LOW LIGHT BOOST",
                            color = Color(0xFFE0AAFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(1.dp))
                }

                if (uiState.isAeAfLocked) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFB300), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { onToggleAeAfLock() }
                    ) {
                        Text(
                            text = "AE/AF LOCK",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // iPhone Style Focus Square and Vertical Sun Exposure Slider
        tapFocusPoint?.let { point ->
            IPhoneFocusExposureControl(
                focusPoint = point,
                focusScale = focusRingScale.value,
                exposureEv = uiState.exposureEv,
                onExposureChanged = onExposureChanged,
                onDismiss = { tapFocusPoint = null }
            )
        }
    }
}

interface CameraCaptureController {
    fun takePhoto()
    fun takeBurstShot(count: Int = 5)
    fun startVideoRecording()
    fun pauseVideoRecording()
    fun resumeVideoRecording()
    fun stopVideoRecording()
}

fun createSyntheticPhotoBitmap(uiState: VinCamUiState): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val shader = android.graphics.LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        intArrayOf(0xFF1E1B2E.toInt(), 0xFF3A2D54.toInt(), 0xFF1E1B2E.toInt()),
        null, android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = shader
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.shader = null

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 38f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("VINCAM V10.9 RETRO", width / 2f, height - 140f, textPaint)

    return applyFilterToBitmap(
        source = bitmap,
        uiState = uiState
    )
}

fun applyFilterToBitmap(
    source: Bitmap,
    uiState: VinCamUiState
): Bitmap {
    val filter = uiState.selectedFilter
    val intensity = uiState.filterIntensity
    val exposureEv = uiState.exposureEv
    val tempOffset = uiState.temperatureOffset
    val overlays = uiState.overlays
    val aspectRatio = uiState.aspectRatio

    val srcWidth = source.width
    val srcHeight = source.height

    val targetRatio = when (aspectRatio) {
        "1:1", "POLAROID" -> 1.0f
        "4:3" -> 3.0f / 4.0f
        "9:16" -> 9.0f / 16.0f
        "2.39:1" -> 2.39f / 1.0f
        else -> 9.0f / 16.0f
    }

    val croppedWidth: Int
    val croppedHeight: Int
    val startX: Int
    val startY: Int

    val srcRatio = srcWidth.toFloat() / srcHeight.toFloat()
    if (srcRatio > targetRatio) {
        croppedHeight = srcHeight
        croppedWidth = (srcHeight * targetRatio).toInt().coerceAtMost(srcWidth)
        startX = (srcWidth - croppedWidth) / 2
        startY = 0
    } else {
        croppedWidth = srcWidth
        croppedHeight = (srcWidth / targetRatio).toInt().coerceAtMost(srcHeight)
        startX = 0
        startY = (srcHeight - croppedHeight) / 2
    }

    val croppedSource = try {
        Bitmap.createBitmap(source, startX, startY, croppedWidth, croppedHeight)
    } catch (e: Exception) {
        source
    }

    val isPolaroid = aspectRatio == "POLAROID"
    val borderPadding = if (isPolaroid) (croppedWidth * 0.06f).toInt() else 0
    val bottomTabHeight = if (isPolaroid) (croppedWidth * 0.18f).toInt() else 0

    val finalWidth = croppedWidth + (borderPadding * 2)
    val finalHeight = croppedHeight + borderPadding + bottomTabHeight

    val result = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    if (isPolaroid) {
        // Fill white Polaroid card background
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, finalWidth.toFloat(), finalHeight.toFloat(), cardPaint)

        // Draw Polaroid date stamp text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textSize = finalWidth * 0.035f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "VINCAM 10.9 • 2026",
            finalWidth / 2f,
            finalHeight - (bottomTabHeight * 0.35f),
            textPaint
        )
    }

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

    // Apply Live CC (Color Correction) Parameters
    if (uiState.ccContrast != 0.0f) {
        val contrastScale = 1.0f + uiState.ccContrast
        val contrastTrans = (-0.5f * contrastScale + 0.5f) * 255f
        arr[0] *= contrastScale
        arr[6] *= contrastScale
        arr[12] *= contrastScale
        arr[4] += contrastTrans
        arr[9] += contrastTrans
        arr[14] += contrastTrans
    }

    if (uiState.ccTint != 0.0f) {
        val tintShift = uiState.ccTint * 0.4f
        arr[4] += tintShift // Magenta
        arr[9] -= tintShift // Green
    }

    paint.colorFilter = ColorMatrixColorFilter(arr)
    canvas.drawBitmap(
        croppedSource,
        borderPadding.toFloat(),
        borderPadding.toFloat(),
        paint
    )

    // Draw selected retro frame onto captured photo bitmap
    val selectedFrame = overlays.find { it.type == OverlayType.FRAME }?.assetNameOrEmoji ?: "None"
    val activeFrame = if (selectedFrame != "None") selectedFrame else if (isPolaroid) "Vintage Polaroid" else "None"
    drawRetroFrameOnBitmapCanvas(canvas, finalWidth, finalHeight, activeFrame, borderPadding, bottomTabHeight)

    // Render Text and Emoji Overlays onto captured bitmap
    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = finalWidth * 0.05f
        color = android.graphics.Color.WHITE
    }

    overlays.filter { it.type != OverlayType.FRAME }.forEach { item ->
        val x = borderPadding + (croppedWidth * item.xRatio)
        val y = borderPadding + (croppedHeight * item.yRatio)
        if (item.textContent.isNotBlank()) {
            canvas.drawText(item.textContent, x, y, overlayPaint)
        } else if (item.assetNameOrEmoji.isNotBlank()) {
            canvas.drawText(item.assetNameOrEmoji, x, y, overlayPaint)
        }
    }

    return result
}

fun drawRetroFrameOnBitmapCanvas(
    canvas: Canvas,
    width: Int,
    height: Int,
    frameName: String,
    borderPadding: Int,
    bottomTabHeight: Int
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    when (frameName) {
        "Vintage Polaroid" -> {
            textPaint.color = android.graphics.Color.DKGRAY
            textPaint.textSize = width * 0.035f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("VINCAM 10.9 • 2026", width / 2f, height - (bottomTabHeight * 0.35f), textPaint)
        }
        "35mm Film Strip", "Kodak 400 Film" -> {
            val border = width * 0.04f
            paint.color = android.graphics.Color.parseColor("#181818")
            canvas.drawRect(0f, 0f, width.toFloat(), border, paint)
            canvas.drawRect(0f, height - border, width.toFloat(), height.toFloat(), paint)
            canvas.drawRect(0f, 0f, border, height.toFloat(), paint)
            canvas.drawRect(width - border, 0f, width.toFloat(), height.toFloat(), paint)

            textPaint.color = android.graphics.Color.parseColor("#FFB300")
            textPaint.textSize = width * 0.025f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("KODAK PORTRA 400 • 35MM SAFETY FILM", width / 2f, border * 0.75f, textPaint)
        }
        "Classic Date Stamp" -> {
            textPaint.color = android.graphics.Color.parseColor("#FF6D00")
            textPaint.textSize = width * 0.045f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("'98 12 25", width - (width * 0.05f), height - (height * 0.05f), textPaint)
        }
        "Cinema Widescreen Border" -> {
            val barH = height * 0.06f
            paint.color = android.graphics.Color.BLACK
            canvas.drawRect(0f, 0f, width.toFloat(), barH, paint)
            canvas.drawRect(0f, height - barH, width.toFloat(), height.toFloat(), paint)
            textPaint.color = android.graphics.Color.parseColor("#00E5FF")
            textPaint.textSize = width * 0.022f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("ANAMORPHIC CINEMA 2.39:1", width / 2f, barH * 0.7f, textPaint)
        }
        "Chunky Retro Frame" -> {
            val frameThickness = width * 0.05f
            paint.color = android.graphics.Color.parseColor("#F0EBE1")
            canvas.drawRect(0f, 0f, width.toFloat(), frameThickness, paint)
            canvas.drawRect(0f, height - frameThickness, width.toFloat(), height.toFloat(), paint)
            canvas.drawRect(0f, 0f, frameThickness, height.toFloat(), paint)
            canvas.drawRect(width - frameThickness, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }
}

@Composable
fun AestheticPhotoFrameOverlay(
    aspectRatio: String,
    frameName: String = "None",
    primaryColor: Color
) {
    val activeFrame = if (frameName != "None") frameName else when (aspectRatio) {
        "POLAROID" -> "Vintage Polaroid"
        "2.39:1" -> "Cinema Widescreen Border"
        else -> "None"
    }

    when (activeFrame) {
        "35mm Film Strip" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(8.dp, Color(0xFF111111), RoundedCornerShape(4.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(Color(0xFF111111))
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(8) {
                        Box(
                            modifier = Modifier
                                .size(8.dp, 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(Color(0xFF111111))
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(8) {
                        Box(
                            modifier = Modifier
                                .size(8.dp, 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
                Text(
                    text = "KODAK SAFETY FILM • 35MM",
                    color = Color(0xFFFFB300),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 1.dp)
                )
            }
        }
        "Kodak 400 Film" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(6.dp, Color(0xFF1E1E1E), RoundedCornerShape(2.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("KODAK PORTRA 400", color = Color(0xFFFFB300), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("• 24A • 36", color = Color(0xFFFFB300), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text("SAFETY FILM", color = Color(0xFFFFB300), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        "Vintage Polaroid" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(10.dp, Color.White, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(34.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VINCAM RETRO • 2026",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
        "Retro Viewfinder" -> {
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeWidth = 2.dp.toPx()
                    val len = 16.dp.toPx()
                    val bracketColor = Color(0xFF00E5FF)

                    drawLine(bracketColor, Offset(0f, 0f), Offset(len, 0f), strokeWidth)
                    drawLine(bracketColor, Offset(0f, 0f), Offset(0f, len), strokeWidth)

                    drawLine(bracketColor, Offset(w, 0f), Offset(w - len, 0f), strokeWidth)
                    drawLine(bracketColor, Offset(w, 0f), Offset(w, len), strokeWidth)

                    drawLine(bracketColor, Offset(0f, h), Offset(len, h), strokeWidth)
                    drawLine(bracketColor, Offset(0f, h), Offset(0f, h - len), strokeWidth)

                    drawLine(bracketColor, Offset(w, h), Offset(w - len, h), strokeWidth)
                    drawLine(bracketColor, Offset(w, h), Offset(w, h - len), strokeWidth)

                    drawLine(Color.White.copy(alpha = 0.5f), Offset(w / 2 - 8.dp.toPx(), h / 2), Offset(w / 2 + 8.dp.toPx(), h / 2), 1.dp.toPx())
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(w / 2, h / 2 - 8.dp.toPx()), Offset(w / 2, h / 2 + 8.dp.toPx()), 1.dp.toPx())
                }

                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• REC", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("🔋 98%", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("AF-C", color = Color(0xFFFFCC00), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("+0.3 EV", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        "Classic Date Stamp" -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "'98 12 25",
                    color = Color(0xFFFF6D00),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                )
            }
        }
        "Cinema Widescreen Border" -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color.Black)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ANAMORPHIC CINEMA • T2.8 • 24 FPS",
                        color = primaryColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color.Black)
                        .align(Alignment.BottomCenter)
                )
            }
        }
        "Chunky Retro Frame" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(12.dp, Color(0xFFF0EBE1), RoundedCornerShape(16.dp))
                    .padding(2.dp)
                    .border(1.5.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
            )
        }
        "CRT Scanlines" -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 6.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += step
                }
            }
        }
    }
}

@Composable
fun IPhoneFocusExposureControl(
    focusPoint: Offset,
    focusScale: Float,
    exposureEv: Float,
    onExposureChanged: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val yellowColor = Color(0xFFFFCC00)
    val density = LocalDensity.current

    val boxSizeDp = 72.dp
    val boxSizePx = with(density) { boxSizeDp.toPx() }

    Box(
        modifier = Modifier
            .size(140.dp)
            .offset {
                IntOffset(
                    (focusPoint.x - boxSizePx / 2f).toInt(),
                    (focusPoint.y - boxSizePx / 2f).toInt()
                )
            }
            .pointerInput(exposureEv) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    // Dragging up increases exposure, dragging down decreases exposure
                    val deltaEv = -dragAmount / 120f
                    val newEv = (exposureEv + deltaEv).coerceIn(-2.0f, 2.0f)
                    onExposureChanged(newEv)
                }
            }
    ) {
        // iPhone Yellow Focus Square Box
        Canvas(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.CenterStart)
        ) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.dp.toPx()
            val cornerLen = 14.dp.toPx()

            // Draw 4 corner brackets
            // Top-Left
            drawLine(yellowColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth)
            drawLine(yellowColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth)

            // Top-Right
            drawLine(yellowColor, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeWidth)
            drawLine(yellowColor, Offset(w, 0f), Offset(w, cornerLen), strokeWidth)

            // Bottom-Left
            drawLine(yellowColor, Offset(0f, h), Offset(cornerLen, h), strokeWidth)
            drawLine(yellowColor, Offset(0f, h), Offset(0f, h - cornerLen), strokeWidth)

            // Bottom-Right
            drawLine(yellowColor, Offset(w, h), Offset(w - cornerLen, h), strokeWidth)
            drawLine(yellowColor, Offset(w, h), Offset(w, h - cornerLen), strokeWidth)
        }

        // Vertical Sun Exposure Slider
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(start = 8.dp)
                .height(90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
                    .background(yellowColor.copy(alpha = 0.6f))
            )
        }

        // Sun Icon positioned along the vertical slider based on exposureEv (-2.0 to +2.0)
        // normalized from 0.0 (top = +2.0 EV) to 1.0 (bottom = -2.0 EV)
        val normalizedEv = (1.0f - (exposureEv.coerceIn(-2.0f, 2.0f) + 2.0f) / 4.0f)
        val sunOffsetY = with(density) { (normalizedEv * 70.dp.toPx()).toInt() }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, sunOffsetY) }
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☀️",
                fontSize = 12.sp
            )
        }
    }
}

