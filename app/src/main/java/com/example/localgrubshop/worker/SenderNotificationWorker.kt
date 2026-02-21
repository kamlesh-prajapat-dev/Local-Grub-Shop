package com.example.localgrubshop.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.NotificationRequest
import com.example.localgrubshop.domain.models.result.UserResult
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SenderNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getString("ORDER_ID") ?: return Result.failure()
        val userID = inputData.getString("USER_ID") ?: return Result.failure()

        return try {
            when(val result = userRepository.getToken(userID)) {
                is UserResult.TokenGetSuccess -> {
                    val token = result.token
                    notificationRepository.sendNotification(
                        NotificationRequest(
                            token = token,
                            title = context.getString(R.string.notification_new_order_title),
                            body = context.getString(R.string.notification_new_order_body, orderId),
                            orderId = orderId
                        )
                    )
                    Result.success()
                }

                is UserResult.Failure -> {
                    Result.failure()
                }
                else -> Result.failure()
            }

        } catch (e: Exception) {
            Log.e("SenderNotificationWorker", "Error sending notification", e)
            Result.retry()
        }
    }
}
