package com.example.localgrubshop.ui.screens.eachorderstatus

import com.example.localgrubshop.data.models.Order

sealed interface EachOrderUIState {
    object Idle : EachOrderUIState
    object Loading : EachOrderUIState
    data class Error(val e: Exception): EachOrderUIState
    data class Success(val data: Boolean): EachOrderUIState
    object NoInternet: EachOrderUIState
    data class OrderGetSuccess(val order: Order): EachOrderUIState
}