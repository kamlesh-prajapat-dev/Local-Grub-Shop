package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.data.models.NotificationRequest
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderHistoryResult
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.collections.sortedByDescending
import kotlin.jvm.java

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository,
) : OrderRepository {

    override suspend fun getOrders(onResult: (OrderHistoryResult) -> Unit) {
        try {
            val snapshot = firestore.collection("orders")
                .get()
                .await()
            val orders = snapshot.documents.map { document ->
                document.toObject(Order::class.java)?.copy(id = document.id) ?: Order()
            }.sortedByDescending { it.placeAt }
            onResult(OrderHistoryResult.Success(orders))
        } catch (e: Exception) {
            onResult(OrderHistoryResult.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override suspend fun updateOrderStatus(
        order: Order,
        newStatus: String,
        onResult: (OrderHistoryResult) -> Unit
    ) {
        try {
            firestore.collection("orders").document(order.id)
                .update("status", newStatus)
                .await()

            CoroutineScope(Dispatchers.IO).launch {
                notificationRepository.sendNotification(
                    NotificationRequest(
                        token = order.token,
                        title = "Order Status Updated",
                        body = "Your order status has been updated to $newStatus",
                        orderId = order.id
                    )
                )
            }

            onResult(OrderHistoryResult.UpdateSuccess(true))
        } catch (e: Exception) {
            onResult(OrderHistoryResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
