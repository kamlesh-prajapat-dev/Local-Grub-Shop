package com.example.localgrubshop.ui.screens.dish

import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure

sealed interface DishUIState {
    object Idle: DishUIState
    object Loading: DishUIState
    object NoInternet: DishUIState
    data class Success(val data: FetchedDish): DishUIState
    data class Failure(val failure: WriteReqDomainFailure): DishUIState
    data class Error(val message: String): DishUIState
}