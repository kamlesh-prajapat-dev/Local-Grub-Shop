package com.example.localgrubshop.ui.screens.offerdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.GetOffer
import com.example.localgrubshop.domain.usecase.OfferUseCase
import com.example.localgrubshop.utils.NetworkUtils
import com.example.localgrubshop.utils.OfferStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class OfferDashboardViewModel @Inject constructor(
    private val offerUseCase: OfferUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<OfferDashboardUIState>(OfferDashboardUIState.Idle)
    val uiState: StateFlow<OfferDashboardUIState> get() = _uiState.asStateFlow()

    private val _activeOffers = MutableStateFlow<List<GetOffer>>(emptyList())
    val activeOffers: StateFlow<List<GetOffer>> = _activeOffers.asStateFlow()

    private val _expiredOffers = MutableStateFlow<List<GetOffer>>(emptyList())
    val expiredOffers: StateFlow<List<GetOffer>> = _expiredOffers.asStateFlow()

    fun onSetOffers(offers: List<GetOffer>) {
        val currentTime = System.currentTimeMillis()

        val (active, expired) = offers.partition { offer ->
            offer.expiryDate >= currentTime &&
                    (offer.offerStatus == OfferStatus.ACTIVE || offer.offerStatus == OfferStatus.INACTIVE)
        }

        _activeOffers.value = active
        _expiredOffers.value = expired.map { it.copy(offerStatus = OfferStatus.EXPIRED) }.sortedBy { it.expiryDate }
    }

    fun fetchOffers() {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OfferDashboardUIState.NoInternet
        }

        offerUseCase.getOffers()
            .onStart {
                _uiState.value = OfferDashboardUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }
}
