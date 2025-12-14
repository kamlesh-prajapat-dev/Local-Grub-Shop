package com.example.localgrubshop.ui.screens.menu

import com.example.localgrubshop.data.models.Dish

sealed class MenuUIState {
    object Idle: MenuUIState()
    object Loading: MenuUIState()
    data class Success(val data: List<Dish>): MenuUIState()
    data class Failure(val message: String): MenuUIState()
    object IsInternetAvailable: MenuUIState()

    data class StockUpdateSuccess(val isSuccess: Boolean): MenuUIState()
    data class DeleteSuccess(val isSuccess: Boolean): MenuUIState()
}