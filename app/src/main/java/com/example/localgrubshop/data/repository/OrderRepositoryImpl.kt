package com.example.localgrubshop.data.repository

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderHistoryResult
import com.example.localgrubshop.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.collections.sortedByDescending
import kotlin.jvm.java

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): OrderRepository {

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
        orderId: String,
        newStatus: String,
        onResult: (OrderHistoryResult) -> Unit
    ) {
        try {
            firestore.collection("orders").document(orderId)
                .update("status", newStatus)
                .await()
            onResult(OrderHistoryResult.UpdateSuccess(true))
        } catch (e: Exception) {
            onResult(OrderHistoryResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
