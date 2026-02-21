package com.example.localgrubshop.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.domain.usecase.ShopOwnerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val shopOwnerUseCase: ShopOwnerUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUIState>(SplashUIState.Idle)
    val uiState: StateFlow<SplashUIState> get() = _uiState.asStateFlow()

    fun loadState() {
        _uiState.value = SplashUIState.Loading

        viewModelScope.launch {
//            delay(2000)
//            _uiState.value = SplashUIState.HomeState

            val user = shopOwnerUseCase.getLocalAdminUser()
            if (user != null) {
                delay(2000)
                _uiState.value = SplashUIState.HomeState
            } else {
                delay(2000)
                _uiState.value = SplashUIState.AuthState
            }
        }
    }

    fun reset() {
        _uiState.value = SplashUIState.Idle
    }
}