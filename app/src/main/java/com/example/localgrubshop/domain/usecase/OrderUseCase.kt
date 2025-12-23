package com.example.localgrubshop.domain.usecase

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.localgrubshop.data.models.NotificationRequest
import com.example.localgrubshop.domain.models.OrderResult
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.domain.repository.UserRepository
import com.example.localgrubshop.ui.screens.eachorderstatus.EachOrderUIState
import com.example.localgrubshop.ui.screens.home.HomeUIState
import com.example.localgrubshop.worker.SenderNotificationWorker
import javax.inject.Inject

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val workManager: WorkManager
) {
    suspend fun getOrders(): HomeUIState {
        return when (val result = orderRepository.getOrders()) {
            is OrderResult.Success -> {
                val orders = result.orders.sortedByDescending { it.placeAt }
                return HomeUIState.Success(orders)
            }

            is OrderResult.Error -> {
                return HomeUIState.Error(result.e)
            }

            is OrderResult.UpdateSuccess -> HomeUIState.Idle
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        userId: String
    ): EachOrderUIState {
        return when (val result = orderRepository.updateOrderStatus(orderId, newStatus)) {
            is OrderResult.UpdateSuccess -> {
                when(val result2 = userRepository.getToken(userId)) {
                    is com.example.localgrubshop.domain.models.UserResult.Success -> {
                        val token = result2.token
                        notificationRepository.sendNotification(
                            NotificationRequest(
                                token = token,
                                title = "Order Status Updated",
                                body = "Your order #$orderId has been updated to $newStatus",
                                orderId = orderId
                            )
                        )
                    }
                    is com.example.localgrubshop.domain.models.UserResult.Error -> {
                        val workRequest = OneTimeWorkRequestBuilder<SenderNotificationWorker>()
                            .setInputData(
                                workDataOf("ORDER_ID" to orderId,
                                    "USER_ID" to userId)
                            )
                            .build()

                        workManager.enqueue(workRequest)
                    }
                }

                EachOrderUIState.Success(result.flag)
            }

            is OrderResult.Error -> {
                return EachOrderUIState.Error(result.e)
            }

            is OrderResult.Success -> EachOrderUIState.Idle
        }
    }
}