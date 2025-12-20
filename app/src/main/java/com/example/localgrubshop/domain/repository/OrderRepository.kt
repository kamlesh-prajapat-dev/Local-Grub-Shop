package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.domain.models.OrderResult


interface OrderRepository {
    suspend fun getOrders(): OrderResult
    suspend fun updateOrderStatus(orderId: String, newStatus: String): OrderResult
}