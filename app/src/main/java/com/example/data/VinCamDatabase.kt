package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CameraPresetEntity

@Database(entities = [CameraPresetEntity::class], version = 1, exportSchema = false)
abstract class VinCamDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: VinCamDatabase? = null

        fun getDatabase(context: Context): VinCamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VinCamDatabase::class.java,
                    "vincam_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
