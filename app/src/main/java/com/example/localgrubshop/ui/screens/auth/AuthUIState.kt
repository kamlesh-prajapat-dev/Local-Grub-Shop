package com.example.localgrubshop.ui.screens.auth

import com.example.localgrubshop.data.models.AdminUser
import com.example.localgrubshop.domain.models.failure.GetReqDomainFailure
import com.example.localgrubshop.domain.models.failure.WriteReqDomainFailure

sealed interface AuthUIState {
    object Idle: AuthUIState
    object Loading: AuthUIState
    data class ValidationError(val validateMsgForUsername: String, val validateMsgForPassword: String): AuthUIState
    data class Success(val adminUser: AdminUser): AuthUIState
    data class Failure(val failure: GetReqDomainFailure): AuthUIState
    data class UpdateTokenSuccess(val isSuccess: Boolean): AuthUIState
    data class UpdateTokenFailure(val failure: WriteReqDomainFailure): AuthUIState
    data class DataLoadFailure(val message: String): AuthUIState
    object NoInternet: AuthUIState
}