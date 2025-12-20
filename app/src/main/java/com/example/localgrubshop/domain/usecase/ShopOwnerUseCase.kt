package com.example.localgrubshop.domain.usecase

import com.example.localgrubshop.domain.models.ShopOwnerResult
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.ui.screens.home.HomeUIState
import javax.inject.Inject

class ShopOwnerUseCase @Inject constructor(
    private val shopOwnerRepository: ShopOwnerRepository
) {
    suspend fun saveFCMToken(token: String): HomeUIState{
        return when(val result = shopOwnerRepository.saveFCMToken(token = token)) {
            is ShopOwnerResult.UpdateSuccess -> {
                return HomeUIState.UpdateSuccess(result.isSuccess)
            }
            is ShopOwnerResult.Error -> {
                return HomeUIState.Error(result.e)
            }
            is ShopOwnerResult.Success -> HomeUIState.Idle
        }
    }
}