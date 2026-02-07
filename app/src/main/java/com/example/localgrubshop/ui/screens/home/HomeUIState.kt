package com.example.localgrubshop.ui.screens.home

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.mapper.firebase.GetReqDomainFailure

sealed interface HomeUIState {
    object Idle : HomeUIState
    object Loading : HomeUIState

    data class Success(val orders: List<Order>) : HomeUIState
    data class Failure(val failure: GetReqDomainFailure) : HomeUIState
    object NoInternet : HomeUIState
}