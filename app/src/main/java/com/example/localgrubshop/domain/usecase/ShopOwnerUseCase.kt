package com.example.localgrubshop.domain.usecase

import com.example.localgrubshop.data.local.LocalDatabase
import com.example.localgrubshop.domain.mapper.firestore.FirestoreFailureMapper
import com.example.localgrubshop.domain.mapper.firestore.FirestoreWriteFailureMapper
import com.example.localgrubshop.domain.models.result.ShopOwnerResult
import com.example.localgrubshop.domain.models.result.TokenResult
import com.example.localgrubshop.domain.repository.ShopOwnerRepository
import com.example.localgrubshop.ui.screens.auth.AuthUIState
import com.example.localgrubshop.utils.AppLogger
import com.example.localgrubshop.utils.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopOwnerUseCase @Inject constructor(
    private val shopOwnerRepository: ShopOwnerRepository,
    private val localDatabase: LocalDatabase
) {
    suspend fun saveFCMToken(token: String, uid: String): AuthUIState{
        return when(val result = shopOwnerRepository.saveFCMToken(token = token, docId = uid)) {
            is ShopOwnerResult.UpdateSuccess -> {
                return AuthUIState.UpdateTokenSuccess(result.isSuccess)
            }
            is ShopOwnerResult.Error -> {
                return AuthUIState.UpdateTokenFailure(FirestoreWriteFailureMapper.map(result.e, uid))
            }
            is ShopOwnerResult.Success -> AuthUIState.Idle
        }
    }

    suspend fun login(username: String, password: String): AuthUIState {
        return when (val result = shopOwnerRepository.login(username, password)) {
            is ShopOwnerResult.Success -> {
                val user = result.adminUser
                localDatabase.setUser(user)

                AuthUIState.Success(user)
            }

            is ShopOwnerResult.Error -> {
                AuthUIState.Failure(FirestoreFailureMapper.map(result.e, username))
            }

            else -> AuthUIState.Idle
        }
    }

    suspend fun getFcmToken(): String {
        return when (val result = TokenManager.getFCMToken()) {
            is TokenResult.Success -> {
                val token = result.token
                if (token != null) {
                    token
                } else {
                    AppLogger.e(
                        tag = "TOKEN_FCM_MANAGER",
                        message = "Token is null"
                    )
                    ""
                }
            }
            is TokenResult.Failure -> {
                AppLogger.e(
                    tag = "TOKEN_FCM_MANAGER",
                    message = "Error getting token",
                    t = result.e
                )
                ""
            }
        }
    }

    fun getLocalAdminUser() = localDatabase.getUser()
}