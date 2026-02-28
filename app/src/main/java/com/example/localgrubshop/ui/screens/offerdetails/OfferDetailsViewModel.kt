package com.example.localgrubshop.ui.screens.offerdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.domain.usecase.OfferUseCase
import com.example.localgrubshop.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferDetailsViewModel @Inject constructor(
    private val offerUseCase: OfferUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<OfferDetailsUIState>(OfferDetailsUIState.Idle)
    val uiState: StateFlow<OfferDetailsUIState> = _uiState.asStateFlow()

    fun updateOfferStatus(offerId: String, status: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OfferDetailsUIState.NoInternet
            return
        }

        _uiState.value = OfferDetailsUIState.Loading
        viewModelScope.launch {
            _uiState.value = offerUseCase.updateOfferStatus(offerId, status)
        }
    }

    fun deleteOffer(offerId: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = OfferDetailsUIState.NoInternet
            return
        }

        _uiState.value = OfferDetailsUIState.Loading
        viewModelScope.launch {
            _uiState.value = offerUseCase.deleteOffer(offerId)
        }
    }

    fun resetState() {
        _uiState.value = OfferDetailsUIState.Idle
    }
}
