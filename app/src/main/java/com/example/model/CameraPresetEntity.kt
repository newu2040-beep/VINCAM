package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camera_presets")
data class CameraPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val filterId: String,
    val filterIntensity: Float,
    val exposure: Float,
    val temperature: Float,
    val tint: Float,
    val contrast: Float,
    val saturation: Float,
    val grain: Float,
    val vignette: Float,
    val frameName: String,
    val textOverlay: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
