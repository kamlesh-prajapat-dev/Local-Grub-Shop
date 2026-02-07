package com.example.localgrubshop.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.domain.usecase.ShopOwnerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val shopOwnerUseCase: ShopOwnerUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow<SplashUIState>(SplashUIState.Idle)
    val uiState: StateFlow<SplashUIState> get() = _uiState.asStateFlow()

    fun loadState() {
        _uiState.value = SplashUIState.Loading

        viewModelScope.launch {
            val user = shopOwnerUseCase.getLocalAdminUser()
            if (user != null) {
                _uiState.value = SplashUIState.HomeState
            } else {
                _uiState.value = SplashUIState.AuthState
            }
        }
     }
}