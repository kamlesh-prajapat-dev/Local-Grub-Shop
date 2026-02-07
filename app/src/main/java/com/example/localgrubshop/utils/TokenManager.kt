package com.example.localgrubshop.utils

import android.util.Log
import com.example.localgrubshop.domain.models.result.TokenResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object TokenManager {
    suspend fun getFCMToken(): TokenResult {
        return try {
            val token =
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("Token", "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    task.result
                }).await()
            TokenResult.Success(token)
        } catch (e: Exception) {
            TokenResult.Failure(e)
        }
    }
}