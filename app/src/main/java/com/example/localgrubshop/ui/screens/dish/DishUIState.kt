package com.example.localgrubshop.ui.screens.dish

import com.example.localgrubshop.data.models.FetchedDish

sealed interface DishUIState {
    object Idle: DishUIState
    object Loading: DishUIState
    object NoInternet: DishUIState
    data class Success(val data: FetchedDish): DishUIState
    data class Failure(val e: Exception): DishUIState
    data class Error(val message: String): DishUIState
}