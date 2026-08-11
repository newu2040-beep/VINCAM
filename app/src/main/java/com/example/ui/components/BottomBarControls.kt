package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.model.CameraMode
import com.example.ui.theme.VinCamRedRecord
import com.example.viewmodel.VinCamUiState

@Composable
fun BottomBarControls(
    uiState: VinCamUiState,
    onModeSelected: (CameraMode) -> Unit,
    onShutterClicked: () -> Unit,
    onPauseResumeVideo: () -> Unit,
    onOpenGallery: () -> Unit,
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
            .padding(bottom = 24.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Mode Selector (VIDEO | PHOTO | PRO) with Geometric Balance dot indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraMode.entries.forEach { mode ->
                val isSelected = uiState.currentMode == mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = mode.title,
                        color = if (isSelected) Color(0xFFFF6321) else Color(0xFFF5F2ED).copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFFF6321) else Color.Transparent)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(VinCamRedRecord.copy(alpha = if (uiState.isRecordingPaused) 0.4f else pulseAlpha))
                )
                val totalSec = uiState.recordingDurationSeconds
                val mins = totalSec / 60
                val secs = totalSec % 60
                val formattedTime = String.format("%02d:%02d", mins, secs)
                Text(
                    text = formattedTime,
                    color = Color(0xFFF5F2ED),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Main Shutter Row: [Gallery Thumbnail] [LARGE GEOMETRIC SHUTTER BUTTON] [LUT Action Button]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Thumbnail Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
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
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF2A2420)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        )
                    }
                }
            }

            // LARGE GEOMETRIC SHUTTER BUTTON
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(4.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .clickable { onShutterClicked() }
                    .testTag("main_shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.currentMode == CameraMode.VIDEO && uiState.isRecording)
                                VinCamRedRecord
                            else
                                Color(0xFFF5F2ED)
                        )
                        .border(2.dp, Color.Black.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.currentMode == CameraMode.VIDEO && uiState.isRecording) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Recording",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Secondary Action (Pause/Resume during Video, or LUT Switcher)
            if (uiState.currentMode == CameraMode.VIDEO && uiState.isRecording) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { onPauseResumeVideo() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isRecordingPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume Recording",
                        tint = Color(0xFFF5F2ED),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { onOpenGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "LUT Filters",
                        tint = Color(0xFFFF6321),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

