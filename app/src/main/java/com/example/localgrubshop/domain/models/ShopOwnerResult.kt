package com.example.localgrubshop.domain.models

sealed interface ShopOwnerResult {
    data class Success(val token: String) : ShopOwnerResult
    data class Error(val e: Exception) : ShopOwnerResult
    data class UpdateSuccess(val isSuccess: Boolean = false) : ShopOwnerResult
}