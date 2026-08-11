package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.MainCameraScreen
import com.example.ui.theme.VinCamTheme
import com.example.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {

    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by cameraViewModel.uiState.collectAsState()
            VinCamTheme(themeOption = uiState.currentTheme) {
                MainCameraScreen(viewModel = cameraViewModel)
            }
        }
    }
}
