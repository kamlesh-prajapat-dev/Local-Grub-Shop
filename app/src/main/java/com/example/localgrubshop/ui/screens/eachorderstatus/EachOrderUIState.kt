package com.example.localgrubshop.ui.screens.eachorderstatus

import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure

sealed interface EachOrderUIState {
    object Idle : EachOrderUIState
    object Loading : EachOrderUIState
    data class GetFailure(val failure: GetReqDomainFailure): EachOrderUIState
    data class WriteFailure(val failure: WriteReqDomainFailure): EachOrderUIState
    data class Success(val data: Boolean): EachOrderUIState
    object NoInternet: EachOrderUIState
    data class OrderGetSuccess(val order: Order): EachOrderUIState
}