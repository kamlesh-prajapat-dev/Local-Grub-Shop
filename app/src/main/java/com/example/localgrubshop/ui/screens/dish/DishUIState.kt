package com.example.localgrubshop.ui.screens.dish

import com.example.localgrubshop.data.models.OldDish

sealed class DishUIState {
    object Idle: DishUIState()
    object Loading: DishUIState()
    object NoInternet: DishUIState()
    data class Success(val data: OldDish): DishUIState()
    data class Failure(val message: String): DishUIState()
}