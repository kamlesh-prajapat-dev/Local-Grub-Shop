package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.domain.models.ShopOwnerResult

interface ShopOwnerRepository {

    suspend fun saveFCMToken(token: String): ShopOwnerResult
    suspend fun getFCMToken(): ShopOwnerResult
}