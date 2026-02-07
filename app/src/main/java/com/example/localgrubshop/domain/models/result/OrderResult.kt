package com.example.localgrubshop.domain.models.result

import com.example.localgrubshop.data.models.Order

sealed class OrderResult {
    data class Success(val orders: List<Order>) : OrderResult()
    data class Failure(val e: Exception) : OrderResult()
    data class UpdateSuccess(val flag: Boolean = false): OrderResult()
    data class OrderGetSuccessByOrderId(val order: Order): OrderResult()
}