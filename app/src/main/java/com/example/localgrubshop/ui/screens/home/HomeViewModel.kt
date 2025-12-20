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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderUseCase: OrderUseCase,
    private val shopOwnerUseCase: ShopOwnerUseCase,
    private val networkUtils: NetworkUtils,
    private val localDatabase: LocalDatabase
) : ViewModel() {

    private val _historyOrder = MutableStateFlow<List<Order>>(emptyList())
    val historyOrder: StateFlow<List<Order>> get() = _historyOrder.asStateFlow()

    fun onSetHistoryOrder(orders: List<Order>) {
        _historyOrder.value = orders
    }

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Idle)
    val uiState: StateFlow<HomeUIState> get() = _uiState.asStateFlow()

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

    fun loadOrderHistoryItems() {
        _uiState.value = HomeUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = HomeUIState.NoInternet
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = orderUseCase.getOrders()
        }
    }
}
