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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val dishUseCase: DishUseCase,
    private val networkUtils: NetworkUtils
): ViewModel() {

    private val _uiState = MutableStateFlow<MenuUIState>(MenuUIState.Idle)
    val uiState: StateFlow<MenuUIState> get() = _uiState.asStateFlow()

    private val _menuItems = MutableStateFlow<List<FetchedDish>>(emptyList())
    val menuItems: StateFlow<List<FetchedDish>> get() = _menuItems.asStateFlow()

    fun onSetMenuItems(newDishes: List<FetchedDish>) {
        _menuItems.update { newDishes }
    }

    fun loadMenu() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { MenuUIState.Loading  }
            if (networkUtils.isInternetAvailable()) {
                val result = dishUseCase.getMenu()
                _uiState.update { result }
            } else {
                _uiState.update { MenuUIState.IsInternetAvailable }
            }
        }
    }

    fun updateStockStatus(dish: FetchedDish, inStock: Boolean) {
        _uiState.update { MenuUIState.Loading }
        if (networkUtils.isInternetAvailable()) {
            viewModelScope.launch {
                val updateStockStatus = dishUseCase.updateStockStatus(dish.id, inStock)
                _uiState.update { updateStockStatus }
            }
        } else {
            _uiState.update { MenuUIState.IsInternetAvailable }
        }
    }

    fun deleteMenuItem(dish: FetchedDish) {
        _uiState.update { MenuUIState.Loading }
        if (networkUtils.isInternetAvailable()) {
            viewModelScope.launch {
                val deleteDish = dishUseCase.deleteDish(dish.id)
                _uiState.update { deleteDish }
            }
        } else {
            _uiState.update { MenuUIState.IsInternetAvailable }
        }
    }
}