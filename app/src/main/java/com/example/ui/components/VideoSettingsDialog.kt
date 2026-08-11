package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BitratePreset
import com.example.model.VideoConfig
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.viewmodel.VinCamUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSettingsBottomSheet(
    uiState: VinCamUiState,
    onUpdateVideoConfig: ((VideoConfig) -> VideoConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val config = uiState.videoConfig

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1B18),
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "VIDEO SETTINGS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Real-Time Hardware Capability Warning & Quick Presets Box
            val isHighQualityUnsupported = config.resolution == VideoResolution.RES_4K || config.fps == VideoFps.FPS_60
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isHighQualityUnsupported) Color(0xFF332000) else Color(0xFF19251E))
                    .border(
                        1.dp,
                        if (isHighQualityUnsupported) Color(0xFFFFB300) else Color(0xFF4CAF50),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isHighQualityUnsupported) "⚠️ HARDWARE COMPATIBILITY NOTICE" else "✅ RECOMMENDED HARDWARE SETTINGS",
                        color = if (isHighQualityUnsupported) Color(0xFFFFCC00) else Color(0xFF81C784),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isHighQualityUnsupported)
                        "Notice: 4K / 60FPS video recording may suffer frame drops or thermal throttling depending on sensor support. 1080p 30FPS is recommended for maximum stability."
                    else
                        "Current configuration is fully supported by this device hardware for smooth, high-frame recording.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )

                // Quick One-Tap Recommended Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E4032))
                            .clickable {
                                onUpdateVideoConfig {
                                    it.copy(
                                        resolution = VideoResolution.RES_1080P,
                                        fps = VideoFps.FPS_30,
                                        bitratePreset = BitratePreset.AUTO
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌟 1080p 30FPS", color = Color(0xFFA5D6A7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                onUpdateVideoConfig {
                                    it.copy(
                                        resolution = VideoResolution.RES_720P,
                                        fps = VideoFps.FPS_30,
                                        bitratePreset = BitratePreset.LOW
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡ 720p 30FPS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Resolution Selector (720p, 1080p, 1440p, 4K)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("RESOLUTION", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoResolution.entries.forEach { res ->
                        val isSel = config.resolution == res
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdateVideoConfig { it.copy(resolution = res) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(res.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FPS Selector (24, 25, 30, 60)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("FPS (FRAME RATE)", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoFps.entries.forEach { fps ->
                        val isSel = config.fps == fps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdateVideoConfig { it.copy(fps = fps) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${fps.fpsValue} fps", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Codec Selector (H.264, H.265)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("VIDEO CODEC", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.model.VideoCodec.entries.forEach { codec ->
                        val isSel = config.codec == codec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdateVideoConfig { it.copy(codec = codec) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(codec.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bitrate Selector (Auto, Low, Medium, High, Maximum, Custom)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("VIDEO BITRATE", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(BitratePreset.AUTO, BitratePreset.LOW, BitratePreset.MEDIUM, BitratePreset.HIGH, BitratePreset.CUSTOM).forEach { bPreset ->
                        val isSel = config.bitratePreset == bPreset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f))
                                .clickable { onUpdateVideoConfig { it.copy(bitratePreset = bPreset) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(bPreset.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (config.bitratePreset == BitratePreset.CUSTOM) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = config.customBitrateMbps.toFloat(),
                            onValueChange = { mbps -> onUpdateVideoConfig { it.copy(customBitrateMbps = mbps.toInt()) } },
                            valueRange = 4f..60f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("${config.customBitrateMbps} Mbps", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Stabilization Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("STABILIZATION", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = config.isStabilizationEnabled,
                    onCheckedChange = { stab -> onUpdateVideoConfig { it.copy(isStabilizationEnabled = stab) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
