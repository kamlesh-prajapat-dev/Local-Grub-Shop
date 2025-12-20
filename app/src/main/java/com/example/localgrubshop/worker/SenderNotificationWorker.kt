package com.example.localgrubshop.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.localgrubshop.data.models.NotificationRequest
import com.example.localgrubshop.domain.models.UserResult
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SenderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getString("ORDER_ID") ?: return Result.failure()
        val userID = inputData.getString("USER_ID") ?: return Result.failure()

        return try {
            when(val result = userRepository.getToken(userID)) {
                is UserResult.Success -> {
                    val token = result.token
                    notificationRepository.sendNotification(
                        NotificationRequest(
                            token = token,
                            title = "New Order Received",
                            body = "You have a new order! Order ID: $orderId",
                            orderId = orderId
                        )
                    )
                    Result.success()
                }

                is UserResult.Error -> {
                    Result.failure()
                }
            }

        } catch (e: Exception) {
            Log.e("SenderNotificationWorker", "Error sending notification", e)
            Result.retry()
        }
    }
}
