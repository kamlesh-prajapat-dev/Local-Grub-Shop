package com.example.localgrubshop.domain.models.result

sealed interface UserResult {
    data class Success(val token: String) : UserResult
    data class Error(val e: Exception) : UserResult
}