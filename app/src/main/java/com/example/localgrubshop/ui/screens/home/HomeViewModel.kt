package com.example.localgrubshop.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.local.LocalHelper
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.domain.models.OrderHistoryResult
import com.example.localgrubshop.domain.repository.OrderRepository
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.utils.NetworkUtils
import com.example.localgrubshop.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val networkUtils: NetworkUtils,
    private val shopOwnerRepository: ShopOwnerRepository,
    private val localHelper: LocalHelper
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    fun onSetIsNetworkAvailable() {
        _isNetworkAvailable.value = true
    }

    init {
        saveFCMToken()
    }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _historyOrder = MutableStateFlow<List<Order>>(emptyList())
    val historyOrder: StateFlow<List<Order>> = _historyOrder

    fun onSetHistoryOrder(orders: List<Order>) {
        _historyOrder.value = orders
    }

    private val _uiState = MutableStateFlow<OrderHistoryResult>(OrderHistoryResult.Idle)
    val uiState: StateFlow<OrderHistoryResult> = _uiState

    fun saveFCMToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val localToken = localHelper.getToken()
            if (localToken != null) {
                val token = async { TokenManager.getFCMToken() }.await()

                if (token != null && token.isNotEmpty() && token != localToken) {
                    shopOwnerRepository.saveFCMToken(token) {
                        if (it) {
                            _errorMessage.value = "Token saved successfully"
                        } else {
                            _errorMessage.value = "Failed to save token"
                        }
                    }
                }
            } else {
                val token = async { TokenManager.getFCMToken() }.await()

                if (token != null && token.isNotEmpty()) {
                    shopOwnerRepository.saveFCMToken(token) {
                        if (it) {
                            _errorMessage.value = "Token saved successfully"
                        } else {
                            _errorMessage.value = "Failed to save token"
                        }
                    }
                }
            }
        }
    }

    fun loadOrderHistoryItems() {
        viewModelScope.launch(Dispatchers.IO) {
            if (networkUtils.isInternetAvailable()) {
                _uiState.value = OrderHistoryResult.Loading
                _isNetworkAvailable.value = true
                orderRepository.getOrders { fetchResult ->
                    _uiState.value = fetchResult
                }
            } else {
                _isNetworkAvailable.value = false
            }
        }
    }
}
