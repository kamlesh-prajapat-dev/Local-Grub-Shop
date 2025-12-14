package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.NotificationRequest
import com.example.localgrubshop.data.remote.NotificationApi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Singleton

@Singleton
class NotificationRepository {

    suspend fun sendNotification(notificationRequest: NotificationRequest): Response<ResponseBody> = NotificationApi.api.sendNotification(notificationRequest)
}