package com.example.localgrubshop.service

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.localgrubshop.utils.NotificationHelper
import com.example.localgrubshop.worker.FCMTokenWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.let

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var workManager: WorkManager

    companion object {
        private const val TAG = "PushNotificationService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val orderId = remoteMessage.data["orderId"] ?: "Unknown Order"
            val body = it.body ?: "Order Status"
            val title = it.title ?: "Order Status"
            notificationHelper.showOrderStatusNotification(applicationContext, orderId, body, title)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        val workRequest = OneTimeWorkRequestBuilder< FCMTokenWorker>()
            .setInputData(
                workDataOf("FCM_TOKEN" to token)
            )
            .build()

        workManager.enqueue(workRequest)
    }
}