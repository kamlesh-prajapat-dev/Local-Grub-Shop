package com.example.localgrubshop.ui.screens.offerbuilder

import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure

sealed interface OfferBuilderUIState {
    object Idle : OfferBuilderUIState
    object Loading : OfferBuilderUIState
    object NoInternet: OfferBuilderUIState
    data class Success(val isSuccess: Boolean) : OfferBuilderUIState
    data class Failure(val failure: WriteReqDomainFailure) : OfferBuilderUIState
    data class ValidationError(val message: String) : OfferBuilderUIState
}
