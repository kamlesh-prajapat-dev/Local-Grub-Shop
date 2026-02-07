package com.example.localgrubshop.domain.models.failure

sealed interface GetReqDomainFailure {
    object NoInternet : GetReqDomainFailure

    data class PermissionDenied(val message: String) : GetReqDomainFailure

    data class DataNotFound(val message: String) : GetReqDomainFailure

    object InvalidRequest : GetReqDomainFailure

    object Cancelled : GetReqDomainFailure

    data class Unknown(
        val cause: Throwable
    ) : GetReqDomainFailure
}
