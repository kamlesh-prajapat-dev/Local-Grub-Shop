package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderHistoryResult


interface OrderRepository {

    suspend fun getOrders(onResult: (OrderHistoryResult) -> Unit)

    suspend fun updateOrderStatus(order: Order, newStatus: String, onResult: (OrderHistoryResult) -> Unit)
}