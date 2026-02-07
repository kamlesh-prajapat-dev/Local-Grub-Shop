package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.domain.models.result.UserResult

interface UserRepository {
    suspend fun getToken(userId: String): UserResult
}