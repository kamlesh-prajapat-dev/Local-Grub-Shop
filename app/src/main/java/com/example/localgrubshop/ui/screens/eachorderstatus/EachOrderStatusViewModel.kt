package com.example.localgrubshop.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.usecase.OrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
) : ViewModel() {

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> get() = _order.asStateFlow()

    private val _uiState = MutableStateFlow<EachOrderUIState>(EachOrderUIState.Idle)
    val uiState: StateFlow<EachOrderUIState> get() = _uiState.asStateFlow()

    fun onSetOrder(order: Order) {
        _order.value = order
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            _uiState.value = EachOrderUIState.Loading
            _uiState.value = orderUseCase.updateOrderStatus(orderId = order.id, newStatus = newStatus, userId = order.userId)
        }
    }

    fun reset() {
        _uiState.value = EachOrderUIState.Idle
    }
}
