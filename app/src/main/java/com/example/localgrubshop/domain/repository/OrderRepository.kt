package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.domain.models.result.OrderResult
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(): Flow<OrderResult>
    suspend fun updateOrderStatus(orderId: String, newStatus: String): OrderResult
    fun observeOrderById(orderId: String): Flow<OrderResult>
}