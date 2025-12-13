package com.example.localgrubshop.domain.models

import com.example.localgrubshop.data.models.Order

sealed class OrderHistoryResult {
    object Idle : OrderHistoryResult()
    object Loading : OrderHistoryResult()
    data class Success(val orders: List<Order>) : OrderHistoryResult()
    data class Error(val message: String) : OrderHistoryResult()
    data class UpdateSuccess(val flag: Boolean): OrderHistoryResult()
}