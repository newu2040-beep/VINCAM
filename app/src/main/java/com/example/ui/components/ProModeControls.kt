package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Iso
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProCameraSettings
import com.example.viewmodel.VinCamUiState

@Composable
fun ProModeControlsOverlay(
    uiState: VinCamUiState,
    onUpdateProSettings: ((ProCameraSettings) -> ProCameraSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeProTab by remember { mutableStateOf<String?>("ISO") } // ISO, SHUTTER, FOCUS, WB, PEAKING, HISTOGRAM

    val pro = uiState.proSettings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Tools Bar: Histogram, Focus Peaking, Zebra, RAW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Histogram Canvas Box
            if (pro.isHistogramEnabled) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                        val path = Path()
                        path.moveTo(0f, size.height)
                        path.quadraticTo(size.width * 0.25f, size.height * 0.2f, size.width * 0.5f, size.height * 0.6f)
                        path.quadraticTo(size.width * 0.75f, size.height * 0.1f, size.width, size.height)
                        drawPath(path, color = Color(0xFF4CAF50), style = Stroke(width = 2.dp.toPx()))
                    }
                }
            } else {
                Box(modifier = Modifier.width(100.dp))
            }

            // Quick Pro Overlay Toggles (Histogram, Peaking, RAW, Zebra)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProChipToggle(
                    label = "HIST",
                    isActive = pro.isHistogramEnabled,
                    onClick = { onUpdateProSettings { it.copy(isHistogramEnabled = !it.isHistogramEnabled) } }
                )
                ProChipToggle(
                    label = "PEAK",
                    isActive = pro.isFocusPeakingEnabled,
                    onClick = { onUpdateProSettings { it.copy(isFocusPeakingEnabled = !it.isFocusPeakingEnabled) } }
                )
                ProChipToggle(
                    label = "ZEBRA",
                    isActive = pro.isZebraStripesEnabled,
                    onClick = { onUpdateProSettings { it.copy(isZebraStripesEnabled = !it.isZebraStripesEnabled) } }
                )
                ProChipToggle(
                    label = "RAW",
                    isActive = pro.isRawEnabled,
                    onClick = { onUpdateProSettings { it.copy(isRawEnabled = !it.isRawEnabled) } }
                )
            }
        }

        // Expanded Control Slider or Option Picker based on activeTab
        AnimatedVisibility(visible = activeProTab != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.80f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                when (activeProTab) {
                    "ISO" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("MANUAL ISO", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val isoValues = listOf("AUTO", "100", "200", "400", "800", "1600", "3200", "6400")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(isoValues) { iso ->
                                    val isSel = pro.iso == iso
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f))
                                            .clickable { onUpdateProSettings { it.copy(iso = iso) } }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(iso, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    "SHUTTER" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SHUTTER SPEED", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val shutterValues = listOf("AUTO", "1/1000s", "1/500s", "1/250s", "1/125s", "1/60s", "1/30s", "1s")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(shutterValues) { shutter ->
                                    val isSel = pro.shutterSpeed == shutter
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f))
                                            .clickable { onUpdateProSettings { it.copy(shutterSpeed = shutter) } }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(shutter, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    "FOCUS" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("MANUAL FOCUS DISTANCE", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(if (pro.focusDistance > 0.8f) "INFINITY" else if (pro.focusDistance < 0.2f) "MACRO" else "%.2f".format(pro.focusDistance), color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Macro", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                Slider(
                                    value = pro.focusDistance,
                                    onValueChange = { dist -> onUpdateProSettings { it.copy(focusDistance = dist, focusMode = "MF") } },
                                    valueRange = 0.0f..1.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                                )
                                Text("Infinity", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                        }
                    }

                    "WB" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("WHITE BALANCE KELVIN", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${pro.wbTemperatureK}K", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = pro.wbTemperatureK.toFloat(),
                                onValueChange = { k -> onUpdateProSettings { it.copy(wbTemperatureK = k.toInt()) } },
                                valueRange = 2500f..7500f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                }
            }
        }

        // Pro Parameter Selector Row: [ISO] [SHUTTER] [FOCUS] [WB]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProTabButton(label = "ISO", valText = pro.iso, isSelected = activeProTab == "ISO") {
                activeProTab = if (activeProTab == "ISO") null else "ISO"
            }
            ProTabButton(label = "SHUTTER", valText = pro.shutterSpeed, isSelected = activeProTab == "SHUTTER") {
                activeProTab = if (activeProTab == "SHUTTER") null else "SHUTTER"
            }
            ProTabButton(label = "FOCUS", valText = pro.focusMode, isSelected = activeProTab == "FOCUS") {
                activeProTab = if (activeProTab == "FOCUS") null else "FOCUS"
            }
            ProTabButton(label = "WB", valText = "${pro.wbTemperatureK}K", isSelected = activeProTab == "WB") {
                activeProTab = if (activeProTab == "WB") null else "WB"
            }
        }
    }
}

@Composable
fun ProChipToggle(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProTabButton(label: String, valText: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(valText, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}
