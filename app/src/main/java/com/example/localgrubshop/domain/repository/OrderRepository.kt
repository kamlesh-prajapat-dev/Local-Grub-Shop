package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.domain.models.OrderHistoryResult


interface OrderRepository {

    suspend fun getOrders(onResult: (OrderHistoryResult) -> Unit)

    suspend fun updateOrderStatus(orderId: String, newStatus: String, onResult: (OrderHistoryResult) -> Unit)
}