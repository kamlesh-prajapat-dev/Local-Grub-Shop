package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.domain.models.result.ShopOwnerResult

interface ShopOwnerRepository {

    suspend fun saveFCMToken(token: String, docId: String): ShopOwnerResult
    suspend fun login(username: String, password: String): ShopOwnerResult
}