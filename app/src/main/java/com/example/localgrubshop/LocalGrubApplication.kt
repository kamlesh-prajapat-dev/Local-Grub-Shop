package com.example.localgrubshop

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LocalGrubApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initCloudinary()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
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