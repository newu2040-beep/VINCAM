package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VinCamUiState

@Composable
fun TopBarControls(
    uiState: VinCamUiState,
    onOpenMenu: () -> Unit,
    onCycleFlash: () -> Unit,
    onCycleTimer: () -> Unit,
    onCycleRatio: () -> Unit,
    onSwitchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity Label (Geometric Balance style with orange indicator)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenMenu() }
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6321))
            )
            Text(
                text = "VINCAM V1.0",
                color = Color(0xFFF5F2ED).copy(alpha = 0.65f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // Quick Controls Pill (Flash, Timer, Ratio, Flip, Menu)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flash Mode Button
            IconButton(
                onClick = onCycleFlash,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .testTag("flash_toggle_button")
            ) {
                val flashIcon = when (uiState.flashMode) {
                    1 -> Icons.Default.FlashAuto
                    2 -> Icons.Default.FlashOn
                    3 -> Icons.Default.Highlight
                    else -> Icons.Default.FlashOff
                }
                val flashTint = if (uiState.flashMode > 0) Color(0xFFFF6321) else Color(0xFFF5F2ED).copy(alpha = 0.8f)
                Icon(
                    imageVector = flashIcon,
                    contentDescription = "Flash Mode",
                    tint = flashTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Timer Button
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(19.dp))
                    .clickable { onCycleTimer() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (uiState.timerSeconds > 0) Color(0xFFFF6321) else Color(0xFFF5F2ED).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    AnimatedVisibility(visible = uiState.timerSeconds > 0) {
                        Text(
                            text = "${uiState.timerSeconds}s",
                            color = Color(0xFFFF6321),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Aspect Ratio Button
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(19.dp))
                    .clickable { onCycleRatio() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.aspectRatio,
                    color = Color(0xFFF5F2ED),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Camera Flip Switch Button
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .testTag("switch_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera Lens",
                    tint = Color(0xFFF5F2ED),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Hamburger Menu Button
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .testTag("hamburger_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = Color(0xFFF5F2ED),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

