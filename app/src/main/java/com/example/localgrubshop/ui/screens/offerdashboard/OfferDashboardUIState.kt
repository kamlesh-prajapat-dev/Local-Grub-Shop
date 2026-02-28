package com.example.localgrubshop.ui.screens.offerdashboard

import com.example.localgrubshop.data.models.GetOffer
import com.example.localgrubshop.domain.mapper.firebase.GetReqDomainFailure

sealed interface OfferDashboardUIState {
    object Idle: OfferDashboardUIState
    object Loading: OfferDashboardUIState
    data class Success(val offers: List<GetOffer>): OfferDashboardUIState
    data class Failure(val failure: GetReqDomainFailure): OfferDashboardUIState
    object NoInternet: OfferDashboardUIState
}
