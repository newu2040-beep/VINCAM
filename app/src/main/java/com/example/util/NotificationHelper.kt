package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "vincam_media_channel"
    private const val CHANNEL_NAME = "VinCam Media Notifications"
    private const val CHANNEL_DESC = "Notifications when photos are captured or videos finish recording"

    private var channelCreated = false

    fun createNotificationChannel(context: Context) {
        if (channelCreated) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        channelCreated = true
    }

    fun showPhotoCapturedNotification(context: Context) {
        createNotificationChannel(context)
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentTitle("📷 Photo Captured & Saved")
                .setContentText("Your retro photo was captured with VinCam and saved to Gallery!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showVideoStoppedNotification(context: Context, durationSeconds: Long) {
        createNotificationChannel(context)
        try {
            val formattedTime = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("📹 Video Recording Stopped & Saved")
                .setContentText("Your vintage video ($formattedTime) was saved to Gallery.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
