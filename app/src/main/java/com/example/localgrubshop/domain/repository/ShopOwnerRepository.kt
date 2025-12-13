package com.example.localgrubshop.domain.repository

interface ShopOwnerRepository {

    suspend fun saveFCMToken(token: String, onResult: (Boolean) -> Unit)
}