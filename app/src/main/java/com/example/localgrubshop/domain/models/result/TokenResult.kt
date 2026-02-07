package com.example.localgrubshop.domain.models.result

sealed interface TokenResult {
    data class Success(val token: String?): TokenResult
    data class Failure(val e: Exception): TokenResult
}