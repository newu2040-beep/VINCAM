package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CameraMode
import com.example.viewmodel.VinCamUiState

@Composable
fun HamburgerDrawerContent(
    uiState: VinCamUiState,
    onNavigate: (sheetOrDialog: String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Branding Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Column {
                Text(
                    text = "VINCAM",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "RETRO CAMERA STUDIO",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.1f))

        Text(
            text = "CAMERA WORKSPACE",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp)
        )

        // Menu Items
        DrawerMenuItem(icon = Icons.Default.Tune, title = "Pro Mode Workspace") { onNavigate("PRO_SETTINGS") }
        DrawerMenuItem(icon = Icons.Default.AutoFixHigh, title = "30+ Built-in LUTs") { onNavigate("LUT") }
        DrawerMenuItem(icon = Icons.Default.Save, title = "Saved Presets DB") { onNavigate("PRESETS") }
        DrawerMenuItem(icon = Icons.Default.CropOriginal, title = "Retro Frames") { onNavigate("OVERLAYS") }
        DrawerMenuItem(icon = Icons.Default.Mood, title = "Stickers & Emojis") { onNavigate("OVERLAYS") }
        DrawerMenuItem(icon = Icons.Default.FontDownload, title = "Typography & Fonts") { onNavigate("OVERLAYS") }
        DrawerMenuItem(icon = Icons.Default.PhotoLibrary, title = "Media Gallery") { onNavigate("GALLERY") }

        Divider(color = Color.White.copy(alpha = 0.1f))

        Text(
            text = "SETTINGS & SYSTEM",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp)
        )

        DrawerMenuItem(icon = Icons.Default.Videocam, title = "Video Recording Settings") { onNavigate("VIDEO_CONFIG") }
        DrawerMenuItem(icon = Icons.Default.ColorLens, title = "13 Retro Themes") { onNavigate("THEMES") }
        DrawerMenuItem(icon = Icons.Default.Folder, title = "Storage & Auto-Save") { onNavigate("STORAGE") }
        DrawerMenuItem(icon = Icons.Default.Notifications, title = "Notifications & Permissions") { onNavigate("PERMISSIONS") }
        DrawerMenuItem(icon = Icons.Default.Lock, title = "Privacy Policy") { onNavigate("PRIVACY") }
        DrawerMenuItem(icon = Icons.Default.HelpOutline, title = "Help & Manual") { onNavigate("HELP") }
        DrawerMenuItem(icon = Icons.Default.Info, title = "About VinCam") { onNavigate("ABOUT") }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Made with ❤️ by Rahul Shah",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
