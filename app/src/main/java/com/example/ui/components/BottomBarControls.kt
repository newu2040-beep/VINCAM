package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.model.CameraMode
import com.example.ui.theme.VincamRedRecord
import com.example.viewmodel.VinCamUiState

@Composable
fun BottomBarControls(
    uiState: VinCamUiState,
    onModeSelected: (CameraMode) -> Unit,
    onShutterClicked: () -> Unit,
    onLongPressShutter: () -> Unit = {},
    onPauseResumeVideo: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenLut: () -> Unit,
    onCycleShutterStyle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Mode Selector (VIDEO | PHOTO | PRO)
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraMode.entries.forEach { mode ->
                val isSelected = uiState.currentMode == mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = mode.title,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                }
            }
        }

        // Live Recording Timer Display (for Video Mode)
        AnimatedVisibility(visible = uiState.currentMode == CameraMode.VIDEO && uiState.isRecording) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(VincamRedRecord.copy(alpha = if (uiState.isRecordingPaused) 0.4f else pulseAlpha))
                )
                val totalSec = uiState.recordingDurationSeconds
                val mins = totalSec / 60
                val secs = totalSec % 60
                val formattedTime = String.format("%02d:%02d", mins, secs)
                Text(
                    text = formattedTime,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Main Shutter Row: [Gallery Thumbnail] [LARGE GEOMETRIC SHUTTER BUTTON] [LUT Action Button]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Thumbnail Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onOpenGallery() }
                    .testTag("gallery_button"),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.lastCapturedUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.lastCapturedUri),
                        contentDescription = "Gallery Thumbnail",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        )
                    }
                }
            }

            // CUSTOMIZABLE AESTHETIC RETRO SHUTTER BUTTON
            CustomShutterButtonComposable(
                uiState = uiState,
                onShutterClicked = onShutterClicked,
                onLongPressShutter = onLongPressShutter,
                onCycleShutterStyle = onCycleShutterStyle
            )

            // Secondary Action (Pause/Resume during Video, or LUT Switcher)
            if (uiState.currentMode == CameraMode.VIDEO && uiState.isRecording) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.05f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onPauseResumeVideo() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isRecordingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume Recording",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.05f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onOpenLut() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "LUT Filters",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomShutterButtonComposable(
    uiState: VinCamUiState,
    onShutterClicked: () -> Unit,
    onLongPressShutter: () -> Unit = {},
    onCycleShutterStyle: () -> Unit
) {
    val isVideoRecording = uiState.currentMode == CameraMode.VIDEO && uiState.isRecording

    val ringColor = when (uiState.shutterButtonStyle) {
        "VINTAGE_CHROME" -> Color(0xFFE0E0E0)
        "PASTEL_ROSE" -> Color(0xFFFFD1DC)
        "LEICA_RED" -> Color(0xFF2A2A2A)
        "NEON_CYAN" -> Color(0xFF00E5FF)
        else -> Color(0xFFFFD700) // CLASSIC_GOLD
    }

    val centerColor = if (isVideoRecording) {
        VincamRedRecord
    } else {
        when (uiState.shutterButtonStyle) {
            "VINTAGE_CHROME" -> Color(0xFFD32F2F)
            "PASTEL_ROSE" -> Color(0xFFFFFDD0)
            "LEICA_RED" -> Color(0xFFE53935)
            "NEON_CYAN" -> Color(0xFF121212)
            else -> MaterialTheme.colorScheme.primary // CLASSIC_GOLD
        }
    }

    Box(
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = ringColor.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(ringColor)
                .combinedClickable(
                    onClick = { onShutterClicked() },
                    onLongClick = { onLongPressShutter() }
                )
                .testTag("main_shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(centerColor),
                contentAlignment = Alignment.Center
            ) {
                if (isVideoRecording) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        // Small shutter style badge button on top right of shutter button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onCycleShutterStyle() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎨",
                fontSize = 12.sp
            )
        }
    }
}


