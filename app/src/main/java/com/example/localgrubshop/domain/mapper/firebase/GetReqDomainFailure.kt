package com.example.localgrubshop.domain.mapper.firebase

sealed class GetReqDomainFailure {
    data class PermissionDenied(val message: String) : GetReqDomainFailure()
    object Network : GetReqDomainFailure()
    data class DataNotFount(val message: String) : GetReqDomainFailure()
    data class InvalidData(val message: String) : GetReqDomainFailure()
    data class Unknown(val cause: Throwable) : GetReqDomainFailure()
}
