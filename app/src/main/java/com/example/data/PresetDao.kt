package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.CameraPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM camera_presets ORDER BY isFavorite DESC, timestamp DESC")
    fun getAllPresets(): Flow<List<CameraPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CameraPresetEntity)

    @Update
    suspend fun updatePreset(preset: CameraPresetEntity)

    @Query("DELETE FROM camera_presets WHERE id = :id")
    suspend fun deletePresetById(id: String)

    @Query("UPDATE camera_presets SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)
}
