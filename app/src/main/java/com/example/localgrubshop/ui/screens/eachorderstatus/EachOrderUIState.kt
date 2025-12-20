package com.example.localgrubshop.ui.screens.eachorderstatus

sealed interface EachOrderUIState {
    object Idle : EachOrderUIState
    object Loading : EachOrderUIState
    data class Error(val e: Exception): EachOrderUIState
    data class Success(val data: Boolean): EachOrderUIState
}