package com.example.localgrubshop

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LocalGrubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initCloudinary()
    }

    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to "dezuhlunc"
        )

        MediaManager.init(this, config)
    }


    private fun createNotificationChannel() {
        val name = "Order Status"
        val descriptionText = "Notifications about your order status"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("ORDER_STATUS_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}