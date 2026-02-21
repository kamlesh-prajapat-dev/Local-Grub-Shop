package com.example.localgrubshop.ui.screens.home

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
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> get() = _orders.asStateFlow()

    private val _filteredList = MutableStateFlow<List<Order>>(emptyList())
    val filteredList: StateFlow<List<Order>> get() = _filteredList.asStateFlow()

    fun onSetOrders(orders: List<Order>) {
        _filteredList.value = orders
        _orders.value = orders
    }
    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Idle)
    val uiState: StateFlow<HomeUIState> get() = _uiState.asStateFlow()

    fun loadOrders() {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = HomeUIState.NoInternet
        }

        orderUseCase.getOrders()
            .onStart {
                _uiState.value = HomeUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }

    fun filterOrders(status: String?, startDate: Date?, endDate: Date?) {
        val currentList = orders.value
        val filteredList = currentList.filter { order ->
            val statusMatch = status == null || order.status == status
            val dateMatch = isDateInRange(Date(order.placeAt), startDate, endDate)
            statusMatch && dateMatch
        }
        _filteredList.value = filteredList
    }
    private fun isDateInRange(
        date: Date,
        startDate: Date?,
        endDate: Date?
    ): Boolean {

        val d = date.clearTime()
        val start = startDate?.clearTime()
        val end = endDate?.clearTime()

        if (start == null && end == null) return true
        if (start != null && end != null) {
            return !d.before(start) && !d.after(end)
        }
        if (start != null) {
            return !d.before(start)
        }
        return !d.after(end)
    }
    private fun Date.clearTime(): Date {
        val cal = Calendar.getInstance()
        cal.time = this
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}
