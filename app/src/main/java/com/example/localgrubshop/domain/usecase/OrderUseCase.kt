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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val workManager: WorkManager
) {
    fun getOrders(): Flow<HomeUIState> {
        return orderRepository.getOrders()
            .map { result ->
                when (result) {
                    is OrderResult.Success -> {
                        val sortedOrders =
                            result.orders.sortedByDescending { it.placeAt }
                        HomeUIState.Success(sortedOrders)
                    }

                    is OrderResult.Error -> {
                        HomeUIState.Error(result.e)
                    }

                    else -> HomeUIState.Idle
                }
            }
            .catch {
                emit(HomeUIState.Error(it as Exception))
            }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        userId: String
    ): EachOrderUIState {
        return when (val result = orderRepository.updateOrderStatus(orderId = orderId, newStatus = newStatus)) {
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

            else -> EachOrderUIState.Idle
        }
    }

    fun observeOrderById(orderId: String): Flow<EachOrderUIState> {
        return orderRepository.observeOrderById(orderId)
            .map { result ->
                when(result) {
                    is OrderResult.OrderGetSuccessByOrderId -> {
                        EachOrderUIState.OrderGetSuccess(result.order)
                    }
                    is OrderResult.Error -> {
                        EachOrderUIState.Error(result.e)
                    }
                    else -> EachOrderUIState.Idle
                }
            }.catch {
                emit(EachOrderUIState.Error(it as Exception))
            }
    }
}