package com.example.localgrubshop.ui.screens.offerdetails

import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure

sealed interface OfferDetailsUIState {
    object Idle: OfferDetailsUIState
    object Loading: OfferDetailsUIState
    object NoInternet: OfferDetailsUIState
    data class DeleteSuccess(val isSuccess: Boolean): OfferDetailsUIState
    data class Failure(val failure: WriteReqDomainFailure): OfferDetailsUIState
    data class StatusUpdateSuccess(val isSuccess: Boolean): OfferDetailsUIState
}