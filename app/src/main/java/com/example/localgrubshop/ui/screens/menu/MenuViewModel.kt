package com.example.localgrubshop.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.usecase.DishUseCase
import com.example.localgrubshop.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val dishUseCase: DishUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<MenuUIState>(MenuUIState.Idle)
    val uiState: StateFlow<MenuUIState> get() = _uiState.asStateFlow()

    private val _menuItems = MutableStateFlow<List<FetchedDish>>(emptyList())
    val menuItems: StateFlow<List<FetchedDish>> get() = _menuItems.asStateFlow()

    fun onSetMenuItems(newDishes: List<FetchedDish>) {
        _menuItems.value = newDishes
    }

    init {
        loadMenu()
    }

    fun loadMenu() {
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = MenuUIState.IsInternetAvailable(null, null)
        }

        dishUseCase.getMenu()
            .onStart {
                _uiState.value = MenuUIState.Loading
            }
            .onEach { state ->
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }

    fun updateStockStatus(dish: FetchedDish, inStock: Boolean) {
        _uiState.value = MenuUIState.Loading
        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = MenuUIState.IsInternetAvailable(dish, inStock)
            return
        }

        viewModelScope.launch {
            val updateStockStatus = dishUseCase.updateStockStatus(dish.id, inStock)
            _uiState.value = updateStockStatus
        }
    }

    fun deleteMenuItem(dish: FetchedDish) {
        _uiState.value = MenuUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = MenuUIState.IsInternetAvailable(null, null)
            return
        }

        viewModelScope.launch {
            val deleteDish = dishUseCase.deleteDish(dish.id)
            _uiState.value = deleteDish
        }
    }

    fun reset() {
        _uiState.value = MenuUIState.Idle
    }
}