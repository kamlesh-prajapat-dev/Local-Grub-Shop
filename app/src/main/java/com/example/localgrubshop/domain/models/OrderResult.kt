package com.example.localgrubshop.domain.models

import com.example.localgrubshop.data.models.Order

sealed class OrderResult {
    data class Success(val orders: List<Order>) : OrderResult()
    data class Error(val e: Exception) : OrderResult()
    data class UpdateSuccess(val flag: Boolean = false): OrderResult()
    data class OrderGetSuccessByOrderId(val order: Order): OrderResult()
}