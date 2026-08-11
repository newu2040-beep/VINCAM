package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.viewmodel.VinCamUiState

@Composable
fun TopBarControls(
    uiState: VinCamUiState,
    onOpenMenu: () -> Unit,
    onCycleFlash: () -> Unit,
    onCycleTimer: () -> Unit,
    onCycleRatio: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTune: () -> Unit,
    onOpenFrames: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings
        TopBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) },
            label = "SETTINGS",
            onClick = onOpenSettings
        )

        // Flash
        val flashIcon = when (uiState.flashMode) {
            1 -> Icons.Default.FlashAuto
            2 -> Icons.Default.FlashOn
            3 -> Icons.Default.Highlight
            else -> Icons.Default.FlashOff
        }
        val flashTint = if (uiState.flashMode > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        TopBarItem(
            icon = { Icon(flashIcon, contentDescription = "Flash", tint = flashTint, modifier = Modifier.size(18.dp)) },
            label = "FLASH",
            onClick = onCycleFlash
        )

        // Timer
        val timerTint = if (uiState.timerSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        TopBarItem(
            icon = { Icon(Icons.Default.Timer, contentDescription = "Timer", tint = timerTint, modifier = Modifier.size(18.dp)) },
            label = if (uiState.timerSeconds > 0) "${uiState.timerSeconds}s" else "TIMER",
            onClick = onCycleTimer
        )

        // Aspect Ratio
        TopBarItem(
            icon = { 
                Text(
                    text = uiState.aspectRatio,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ) 
            },
            label = "RATIO",
            onClick = onCycleRatio,
            isPill = true
        )

        // Retro Frames Quick Button
        TopBarItem(
            icon = { Icon(Icons.Default.CropOriginal, contentDescription = "Frames", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
            label = "FRAMES",
            onClick = onOpenFrames,
            isAccent = true
        )

        // Quality/Resolution
        val qualityText = if (uiState.currentMode == CameraMode.VIDEO) {
            "${uiState.videoConfig.resolution.label}"
        } else "RAW"
        TopBarItem(
            icon = { 
                Text(
                    text = qualityText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ) 
            },
            label = "RES",
            onClick = onOpenSettings,
            isAccent = true,
            isPill = true
        )

        // Flip Camera
        TopBarItem(
            icon = { Icon(Icons.Default.Cameraswitch, contentDescription = "Flip", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) },
            label = "FLIP",
            onClick = onSwitchCamera
        )

        // Menu
        TopBarItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) },
            label = "MENU",
            onClick = onOpenMenu
        )
    }
}

@Composable
fun TopBarItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    isAccent: Boolean = false,
    isPill: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val shape = if (isPill) RoundedCornerShape(16.dp) else CircleShape
        val bgColor = if (isAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        
        Box(
            modifier = Modifier
                .height(36.dp)
                .then(if (isPill) Modifier.padding(horizontal = 2.dp) else Modifier.size(36.dp))
                .shadow(elevation = 4.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.05f))
                .clip(shape)
                .background(bgColor)
                .clickable(onClick = onClick)
                .padding(horizontal = if (isPill) 12.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}




