package com.example.localgrubshop.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.usecase.OrderUseCase
import com.example.localgrubshop.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase, // Order Use case field
    private val networkUtils: NetworkUtils //
) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> get() = _order.asStateFlow()

    private val _uiState = MutableStateFlow<EachOrderUIState>(EachOrderUIState.Idle)
    val uiState: StateFlow<EachOrderUIState> get() = _uiState.asStateFlow()

    fun onSetOrder(order: Order?) {
        _order.value = order
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        _uiState.value = EachOrderUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = EachOrderUIState.NoInternet
            return
        }

        viewModelScope.launch {
            _uiState.value = orderUseCase.updateOrderStatus(orderId = order.id, newStatus = newStatus, userId = order.userId)
        }
    }

    fun observeOrderById(orderId: String) {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = EachOrderUIState.NoInternet
            return
        }

        orderUseCase.observeOrderById(orderId)
            .onStart {
                _uiState.value = EachOrderUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state

            }
            .launchIn(viewModelScope)
    }

    fun reset() {
        _uiState.value = EachOrderUIState.Idle
    }
}
