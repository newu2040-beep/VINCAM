package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OverlayItem
import com.example.model.OverlayType
import com.example.viewmodel.VinCamUiState

@Composable
fun OverlayCanvas(
    uiState: VinCamUiState,
    onSelectOverlay: (String?) -> Unit,
    onUpdatePosition: (id: String, xRatio: Float, yRatio: Float) -> Unit,
    onUpdateTransform: (id: String, scaleDelta: Float, rotationDelta: Float) -> Unit,
    onDeleteOverlay: (id: String) -> Unit,
    onDuplicateOverlay: (id: String) -> Unit,
    onToggleLock: (id: String) -> Unit,
    onSetOpacity: (id: String, opacity: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clickable { onSelectOverlay(null) }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        uiState.overlays.filter { it.type != OverlayType.FRAME }.sortedBy { it.zIndex }.forEach { item ->
            val isSelected = uiState.selectedOverlayId == item.id
            val offsetX = (item.xRatio * widthPx).toInt()
            val offsetY = (item.yRatio * heightPx).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .rotate(item.rotation)
                    .scale(item.scale)
                    .alpha(item.opacity)
                    .pointerInput(item.id, item.isLocked) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            if (!item.isLocked) {
                                val newX = item.xRatio + (pan.x / widthPx)
                                val newY = item.yRatio + (pan.y / heightPx)
                                onUpdatePosition(item.id, newX, newY)
                                if (zoom != 1f || rotation != 0f) {
                                    onUpdateTransform(item.id, zoom, rotation)
                                }
                            }
                        }
                    }
                    .clickable { onSelectOverlay(item.id) }
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                when (item.type) {
                    OverlayType.TEXT -> {
                        Text(
                            text = item.textContent,
                            color = Color.White,
                            fontSize = item.fontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                    }
                    OverlayType.EMOJI, OverlayType.STICKER -> {
                        if (item.assetNameOrEmoji.length <= 4) {
                            Text(
                                text = item.assetNameOrEmoji,
                                fontSize = 42.sp
                            )
                        } else {
                            Text(
                                text = item.assetNameOrEmoji,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    OverlayType.FRAME -> {
                        Box(
                            modifier = Modifier
                                .size(280.dp, 360.dp)
                                .border(8.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        )
                    }
                    else -> {}
                }
            }

            // Contextual Overlay Controls when selected
            if (isSelected) {
                Column(
                    modifier = Modifier
                        .offset { IntOffset(offsetX, (offsetY - 50.dp.toPx()).toInt()) }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleLock(item.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDuplicateOverlay(item.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteOverlay(item.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
