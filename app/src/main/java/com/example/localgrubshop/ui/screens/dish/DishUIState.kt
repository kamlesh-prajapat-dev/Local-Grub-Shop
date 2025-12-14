package com.example.localgrubshop.ui.screens.dish

import com.example.localgrubshop.data.models.Dish

sealed class DishUIState {
    object Idle: DishUIState()
    object Loading: DishUIState()
    object NoInternet: DishUIState()
    data class Success(val data: Dish): DishUIState()
    data class Failure(val message: String): DishUIState()
}