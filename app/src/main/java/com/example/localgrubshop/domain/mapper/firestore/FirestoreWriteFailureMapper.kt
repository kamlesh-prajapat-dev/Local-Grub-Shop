package com.example.localgrubshop.domain.mapper.firestore

import com.example.localgrubshop.domain.models.failure.WriteReqDomainFailure
import com.example.localgrubshop.utils.AppLogger
import com.google.firebase.firestore.FirebaseFirestoreException

object FirestoreWriteFailureMapper {

    fun <T> map(throwable: Throwable, data: T): WriteReqDomainFailure {
        return when(throwable) {
            is FirebaseFirestoreException -> {
                mapFirebaseFirestoreException(throwable, data)
            }
            is IllegalArgumentException -> {
                WriteReqDomainFailure.InvalidArgument
            }
            else -> {
                AppLogger.e(
                    "UnknownFailure",
                    "Unexpected error: data is $data",
                    t = throwable
                )
                WriteReqDomainFailure.Unknown(throwable)
            }
        }
    }

    fun <T> mapFirebaseFirestoreException(
        e: FirebaseFirestoreException,
        data: T
    ): WriteReqDomainFailure {
        return when (e.code) {

            FirebaseFirestoreException.Code.OK ->
                WriteReqDomainFailure.Unknown(e) // write shouldn't fail with OK

            FirebaseFirestoreException.Code.CANCELLED ->
                WriteReqDomainFailure.Cancelled(e.message ?: "Cancelled")

            FirebaseFirestoreException.Code.UNKNOWN -> {
                AppLogger.e(
                    "UnknownFailure",
                    "Unexpected error: data is $data",
                    t = e.cause
                )
                WriteReqDomainFailure.Unknown(e)
            }

            FirebaseFirestoreException.Code.INVALID_ARGUMENT ->
                WriteReqDomainFailure.InvalidArgument

            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                WriteReqDomainFailure.DeadlineExceeded

            FirebaseFirestoreException.Code.NOT_FOUND ->
                WriteReqDomainFailure.NotFound(e.message ?: "Not Found")

            FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                WriteReqDomainFailure.AlreadyExists(e.message ?: "Already Exists")

            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                WriteReqDomainFailure.PermissionDenied(e.message ?: "Permission Denied")

            FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                WriteReqDomainFailure.Unauthenticated(e.message ?: "Unauthenticated")

            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                WriteReqDomainFailure.ResourceExhausted

            FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                WriteReqDomainFailure.FailedPrecondition

            FirebaseFirestoreException.Code.ABORTED ->
                WriteReqDomainFailure.Aborted

            FirebaseFirestoreException.Code.OUT_OF_RANGE ->
                WriteReqDomainFailure.OutOfRange

            FirebaseFirestoreException.Code.UNIMPLEMENTED ->
                WriteReqDomainFailure.Unimplemented

            FirebaseFirestoreException.Code.INTERNAL ->
                WriteReqDomainFailure.Internal

            FirebaseFirestoreException.Code.UNAVAILABLE ->
                WriteReqDomainFailure.NetworkUnavailable

            FirebaseFirestoreException.Code.DATA_LOSS ->
                WriteReqDomainFailure.DataLoss
        }
    }
}
