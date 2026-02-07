package com.example.localgrubshop.domain.models.failure

sealed interface WriteReqDomainFailure {

    // Connectivity & system state
    object NetworkUnavailable : WriteReqDomainFailure
    object DeadlineExceeded : WriteReqDomainFailure
    object ResourceExhausted : WriteReqDomainFailure
    data class Cancelled(val message: String) : WriteReqDomainFailure

    // Auth & security
    data class PermissionDenied(val message: String) : WriteReqDomainFailure
    data class Unauthenticated(val message: String): WriteReqDomainFailure

    // Data & request issues
    data class NotFound(val message: String) : WriteReqDomainFailure
    data class AlreadyExists(val message: String) : WriteReqDomainFailure
    object InvalidArgument : WriteReqDomainFailure
    object FailedPrecondition : WriteReqDomainFailure
    object OutOfRange : WriteReqDomainFailure

    // Server-side / internal
    object Internal : WriteReqDomainFailure
    object Unimplemented : WriteReqDomainFailure
    object Aborted : WriteReqDomainFailure
    object DataLoss : WriteReqDomainFailure

    // Absolute fallback (should be extremely rare)
    data class Unknown(
        val cause: Throwable
    ) : WriteReqDomainFailure
}