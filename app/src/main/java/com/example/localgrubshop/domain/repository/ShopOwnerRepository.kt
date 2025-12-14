package com.example.localgrubshop.domain.repository

interface ShopOwnerRepository {

    suspend fun saveFCMToken(token: String, onResult: (Boolean) -> Unit)
    suspend fun getFCMToken(onResult: (String) -> Unit)
}