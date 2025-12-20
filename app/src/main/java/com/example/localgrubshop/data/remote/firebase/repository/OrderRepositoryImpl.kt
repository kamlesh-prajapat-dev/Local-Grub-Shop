package com.example.localgrubshop.data.remote.firebase.repository

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderResult
import com.example.localgrubshop.domain.repository.NotificationRepository
import com.example.localgrubshop.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.collections.sortedByDescending
import kotlin.jvm.java

class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    override suspend fun getOrders(): OrderResult {
        return try {
            val snapshot = firestore.collection("orders")
                .get()
                .await()
            val documents = snapshot.documents
            if (documents.isNotEmpty()) {
                val orders = documents.map { document ->
                    document.toObject(Order::class.java)?.copy(id = document.id) ?: Order()
                }
                OrderResult.Success(orders)
            } else {
                OrderResult.Success(emptyList())
            }
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String
    ): OrderResult {
        return try {
            firestore.collection("orders").document(orderId)
                .update("status", newStatus)
                .await()

            OrderResult.UpdateSuccess(true)
        } catch (e: Exception) {
            OrderResult.Error(e)
        }
    }
}
