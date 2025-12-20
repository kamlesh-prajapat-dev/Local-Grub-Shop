package com.example.localgrubshop.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.example.localgrubshop.domain.usecase.ShopOwnerUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FCMTokenWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val shopOwnerUseCase: ShopOwnerUseCase
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val token = inputData.getString("FCM_TOKEN") ?: return Result.failure()

        return try {
            shopOwnerUseCase.saveFCMToken(token)
            Result.success()
        } catch (e: Exception) {
            Log.e("FCMTokenWorker", "Error saving FCM token", e)
            Result.retry()
        }
    }
}