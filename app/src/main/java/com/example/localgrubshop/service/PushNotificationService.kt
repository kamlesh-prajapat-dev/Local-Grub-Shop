package com.example.localgrubshop.service

import android.util.Log
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.let

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var shopOwnerRepository: ShopOwnerRepository

    companion object {
        private const val TAG = "PushNotificationService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            // For simplicity, we'll use the orderId from the data payload
            val orderId = remoteMessage.data["orderId"] ?: "Unknown Order"
            val status = it.body ?: ""
            notificationHelper.showOrderStatusNotification(applicationContext, orderId, status)
        }
    }

    override fun onNewToken(token: String) {
        // This is where you would send the token to your server.
        // For now, we'll just log it.
        Log.d(TAG, "Refreshed token: $token")


        CoroutineScope(Dispatchers.IO).launch {
            shopOwnerRepository.saveFCMToken(token) {
                if (it) {
                    Log.d(TAG, "New token saved successfully")
                } else {
                    Log.e(TAG, "Failed to save new token")
                }
            }
        }
    }
}