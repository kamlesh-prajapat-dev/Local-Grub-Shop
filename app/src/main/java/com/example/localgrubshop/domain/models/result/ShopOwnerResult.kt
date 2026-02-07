package com.example.localgrubshop.domain.models.result

import com.example.localgrubshop.data.models.AdminUser

sealed interface ShopOwnerResult {
    data class Success(val adminUser: AdminUser) : ShopOwnerResult
    data class Error(val e: Exception) : ShopOwnerResult
    data class UpdateSuccess(val isSuccess: Boolean) : ShopOwnerResult
}