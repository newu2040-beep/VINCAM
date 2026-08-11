package com.example.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateTaken: Long,
    val isVideo: Boolean,
    val durationMs: Long = 0,
    val sizeBytes: Long = 0
)

class MediaStoreRepository(private val context: Context) {

    suspend fun savePhotoToMediaStore(bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "VINCAM_$timestamp.jpg"

        val localFolder = File(context.filesDir, "VinCamMedia").apply { mkdirs() }
        val localFile = File(localFolder, filename)
        try {
            localFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/VinCam")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val imageUri = try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: Exception) {
            null
        }

        if (imageUri != null) {
            try {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            } catch (e: Exception) { }
        }

        imageUri ?: Uri.fromFile(localFile)
    }

    suspend fun saveVideoFileToMediaStore(videoFile: File): Uri? = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "VINCAM_$timestamp.mp4"

        val localFolder = File(context.filesDir, "VinCamMedia").apply { mkdirs() }
        val localFile = File(localFolder, filename)
        try {
            videoFile.inputStream().use { input ->
                localFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VinCam")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val videoUri = try {
            resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: Exception) {
            null
        }

        if (videoUri != null) {
            try {
                resolver.openOutputStream(videoUri)?.use { outputStream ->
                    videoFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(videoUri, contentValues, null, null)
                }
            } catch (e: Exception) { }
        }

        videoUri ?: Uri.fromFile(localFile)
    }

    suspend fun getRecentMediaItems(limit: Int = 50): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        val existingNames = mutableSetOf<String>()

        // 1. Local VinCamMedia folder items
        val localFolder = File(context.filesDir, "VinCamMedia")
        if (localFolder.exists()) {
            localFolder.listFiles()?.forEach { file ->
                val name = file.name
                val isVideo = name.endsWith(".mp4", ignoreCase = true)
                val isJpg = name.endsWith(".jpg", ignoreCase = true) || name.endsWith(".jpeg", ignoreCase = true)
                if (isVideo || isJpg) {
                    items.add(
                        MediaItem(
                            id = file.hashCode().toLong(),
                            uri = Uri.fromFile(file),
                            name = name,
                            dateTaken = file.lastModified(),
                            isVideo = isVideo,
                            sizeBytes = file.length()
                        )
                    )
                    existingNames.add(name)
                }
            }
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.SIZE
        )

        // Query Images from MediaStore
        try {
            val imageQuery = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            )
            imageQuery?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn) ?: "Photo"
                    if (!existingNames.contains(name)) {
                        val id = cursor.getLong(idColumn)
                        val date = cursor.getLong(dateColumn)
                        val size = cursor.getLong(sizeColumn)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        items.add(MediaItem(id, contentUri, name, date, isVideo = false, sizeBytes = size))
                        existingNames.add(name)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Query Videos from MediaStore
        try {
            val videoQuery = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_TAKEN} DESC"
            )
            videoQuery?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameColumn) ?: "Video"
                    if (!existingNames.contains(name)) {
                        val id = cursor.getLong(idColumn)
                        val date = cursor.getLong(dateColumn)
                        val size = cursor.getLong(sizeColumn)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                        items.add(MediaItem(id, contentUri, name, date, isVideo = true, sizeBytes = size))
                        existingNames.add(name)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        items.sortedByDescending { it.dateTaken }.take(limit)
    }
}
