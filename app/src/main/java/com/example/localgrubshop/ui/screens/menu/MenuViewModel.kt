package com.example.localgrubshop.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.OldDish
import com.example.localgrubshop.domain.repository.MenuRepository
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
    private val menuRepository: MenuRepository,
    private val networkUtils: NetworkUtils
): ViewModel() {

    private val _uiState = MutableStateFlow<MenuUIState>(MenuUIState.Idle)
    val uiState: StateFlow<MenuUIState> get() = _uiState.asStateFlow()

    private val _menuItems = MutableStateFlow<List<OldDish>>(emptyList())
    val menuItems: StateFlow<List<OldDish>> get() = _menuItems.asStateFlow()

    fun onSetMenuItems(newDishes: List<OldDish>) {
        _menuItems.update { newDishes }
    }

    fun loadMenu() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { MenuUIState.Loading  }
            if (networkUtils.isInternetAvailable()) {
                val result = menuRepository.getMenu()
                _uiState.update { result }
            } else {
                _uiState.update { MenuUIState.IsInternetAvailable }
            }
        }
    }

    fun updateStockStatus(dish: OldDish, inStock: Boolean) {
        _uiState.update { MenuUIState.Loading }
        if (networkUtils.isInternetAvailable()) {
            viewModelScope.launch {
                val updateStockStatus = menuRepository.updateStockStatus(dish.id, inStock)
                _uiState.update { updateStockStatus }
            }
        } else {
            _uiState.update { MenuUIState.IsInternetAvailable }
        }
    }

    fun deleteMenuItem(dish: OldDish) {
        _uiState.update { MenuUIState.Loading }
        if (networkUtils.isInternetAvailable()) {
            viewModelScope.launch {
                val deleteDish = menuRepository.deleteDish(dish.id)
                _uiState.update { deleteDish }
            }
        } else {
            _uiState.update { MenuUIState.IsInternetAvailable }
        }
    }
}