package com.example.localgrubshop.ui.screens.home

import com.example.localgrubshop.data.models.Order

sealed interface HomeUIState {
    object Idle : HomeUIState
    object Loading : HomeUIState

    data class Success(val orders: List<Order>) : HomeUIState
    data class Error(val e: Exception) : HomeUIState
    data class UpdateSuccess(val flag: Boolean): HomeUIState
    object NoInternet : HomeUIState
}