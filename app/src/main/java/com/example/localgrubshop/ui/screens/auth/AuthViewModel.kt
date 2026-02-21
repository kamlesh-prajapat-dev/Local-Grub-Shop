package com.example.localgrubshop.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localgrubshop.data.models.AdminUser
import com.example.localgrubshop.domain.usecase.ShopOwnerUseCase
import com.example.localgrubshop.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val shopOwnerUseCase: ShopOwnerUseCase,
    private val networkUtils: NetworkUtils
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUIState>(AuthUIState.Idle)
    val uiState: StateFlow<AuthUIState> get() = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        _uiState.value = AuthUIState.Loading

        val validateMsgForUsername = validateUsername(username)
        val validateMsgForPassword = validatePassword(password)

        if (validateMsgForUsername != null || validateMsgForPassword != null) {
            _uiState.value = AuthUIState.ValidationError(
                validateMsgForUsername = validateMsgForUsername ?: "",
                validateMsgForPassword = validateMsgForPassword ?: ""
            )
            return
        }

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = AuthUIState.NoInternet
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = shopOwnerUseCase.login(username, password)
            _uiState.value = result
        }
    }

    fun saveToken(user: AdminUser) {
        _uiState.value = AuthUIState.Loading

        if (!networkUtils.isInternetAvailable()) {
            _uiState.value = AuthUIState.NoInternet
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val token = shopOwnerUseCase.getFcmToken()
            if (token.isNotEmpty()) {
                _uiState.value = shopOwnerUseCase.saveFCMToken(token, user.id)
            } else {
                _uiState.value = AuthUIState.DataLoadFailure("Something went wrong. Please try again after some time.")
            }
        }
    }

    private fun validateUsername(username: String): String? {
        if (username.isBlank()) {
            return "Username cannot be empty"
        }

        if (username.length < 3) {
            return "Username must be at least 3 characters long"
        }

        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Password cannot be empty"
        }

        if (password.length < 6) {
            return "Password must be at least 6 characters long"
        }
        return null
    }
}