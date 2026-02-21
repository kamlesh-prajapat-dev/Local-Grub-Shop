package com.example.localgrubshop.domain.usecase

import com.example.localgrubshop.domain.mapper.firestore.FirestoreFailureMapper
import com.example.localgrubshop.domain.models.result.UserResult
import com.example.localgrubshop.domain.repository.UserRepository
import com.example.localgrubshop.ui.screens.home.HomeUIState
import javax.inject.Inject

class UserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun getUserByUid(uid: String): HomeUIState {
        return when (val result = userRepository.getUserByUid(uid)) {
            is UserResult.Failure -> {
                HomeUIState.UserGetFailure(FirestoreFailureMapper.map(result.e, uid))
            }

            is UserResult.UserGetSuccess -> {
                HomeUIState.UserGetSuccess(result.user)
            }
            
            else -> HomeUIState.Idle
        }
    }
}