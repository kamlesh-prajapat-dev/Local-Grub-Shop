package com.example.localgrubshop.ui.screens.eachorderstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderHistoryResult
import com.example.localgrubshop.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EachOrderStatusViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _updateStatusResult = MutableStateFlow<OrderHistoryResult>(OrderHistoryResult.Idle)
    val updateStatusResult: StateFlow<OrderHistoryResult> = _updateStatusResult.asStateFlow()

    fun onSetStatusResult(statusResult: OrderHistoryResult) {
        _updateStatusResult.value = statusResult
    }

    fun onSetOrder(order: Order) {
        _order.value = order
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            _updateStatusResult.value = OrderHistoryResult.Loading
            orderRepository.updateOrderStatus(order, newStatus) {
                _updateStatusResult.value = it
            }
        }
    }
}
