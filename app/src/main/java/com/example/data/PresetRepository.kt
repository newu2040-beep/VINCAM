package com.example.data

import com.example.model.CameraPresetEntity
import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<CameraPresetEntity>> = presetDao.getAllPresets()

    suspend fun savePreset(preset: CameraPresetEntity) {
        presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(id: String) {
        presetDao.deletePresetById(id)
    }

    suspend fun toggleFavorite(id: String, currentFavorite: Boolean) {
        presetDao.setFavorite(id, !currentFavorite)
    }
}
