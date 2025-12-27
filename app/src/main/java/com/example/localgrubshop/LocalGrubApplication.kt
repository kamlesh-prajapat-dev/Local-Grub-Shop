package com.example.localgrubshop

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.localgrubshop.BuildConfig
import com.google.firebase.database.FirebaseDatabase

@HiltAndroidApp
class LocalGrubApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initCloudinary()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun initCloudinary() {
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME
        )

        MediaManager.init(this, config)
    }


    private fun createNotificationChannel() {
        val name = getString(R.string.notification_channel_name)
        val descriptionText = getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(ORDER_STATUS_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ORDER_STATUS_CHANNEL_ID = "ORDER_STATUS_CHANNEL"
    }
}