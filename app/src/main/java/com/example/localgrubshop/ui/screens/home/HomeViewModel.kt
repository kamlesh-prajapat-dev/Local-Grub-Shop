package com.example.localgrubshop.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.local.LocalDatabase
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.usecase.OrderUseCase
import com.example.localgrubshop.domain.usecase.ShopOwnerUseCase
import com.example.localgrubshop.utils.NetworkUtils
import com.example.localgrubshop.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val shopOwnerUseCase: ShopOwnerUseCase,
    private val networkUtils: NetworkUtils,
    private val localDatabase: LocalDatabase
) : ViewModel() {
    // StateFlow for history order that will be observed by HomeFragment
    private val _historyOrder = MutableStateFlow<List<Order>>(emptyList())
    val historyOrder: StateFlow<List<Order>> get() = _historyOrder.asStateFlow()
    // method to update history order
    fun onSetHistoryOrder(orders: List<Order>) {
        _historyOrder.value = orders
    }
    // StateFlow for UI state that will be observed by HomeFragment and update UI accordingly
    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Idle)
    val uiState: StateFlow<HomeUIState> get() = _uiState.asStateFlow()

    init {
        loadOrderHistoryItems()
    }

    // method to load FCM token from local database and if not present then fetch from firebase, save it to local database
    fun saveFCMToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val localToken = localDatabase.getToken()
            if (localToken == null) {
                val token = async { TokenManager.getFCMToken() }.await()

                if (token != null && token.isNotEmpty()) {
                    _uiState.value = shopOwnerUseCase.saveFCMToken(token)
                }
            }
        }
    }
    // method to load order history items from firebase and update UI accordingly
    fun loadOrderHistoryItems() {
        _uiState.value = HomeUIState.Loading

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
    // method to filter orders based on status and date range
    fun filterOrders(status: String?, startDate: Date?, endDate: Date?) {
        val currentList = historyOrder.value
        val filteredList = currentList.filter { order ->
            val statusMatch = status == null || order.status == status
            val dateMatch = isDateInRange(Date(order.placeAt), startDate, endDate)
            statusMatch && dateMatch
        }
        _historyOrder.value = filteredList
    }
    // method to check if date is in range between start date and end date
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
    // method to clear time from date
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
