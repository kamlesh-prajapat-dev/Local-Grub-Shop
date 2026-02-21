package com.example.localgrubshop.domain.models.result

import com.example.localgrubshop.data.models.User

sealed interface UserResult {
    data class TokenGetSuccess(val token: String) : UserResult
    data class UserGetSuccess(val user: User): UserResult
    data class Failure(val e: Exception) : UserResult
}