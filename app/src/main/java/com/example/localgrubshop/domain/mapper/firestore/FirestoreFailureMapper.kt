package com.example.localgrubshop.domain.mapper.firestore

import com.example.localgrubshop.domain.models.failure.GetReqDomainFailure
import com.example.localgrubshop.utils.AppLogger
import com.example.localgrubshop.utils.DataNotFoundException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlin.coroutines.cancellation.CancellationException

object FirestoreFailureMapper {

    fun <T> map(throwable: Throwable, data: T): GetReqDomainFailure {

        return when (throwable) {

            is CancellationException -> {
                GetReqDomainFailure.Cancelled
            }

            is FirebaseNetworkException -> {
                GetReqDomainFailure.NoInternet
            }

            is FirebaseFirestoreException -> {
                mapFirestoreException(throwable, data)
            }

            is IllegalArgumentException -> {
                GetReqDomainFailure.InvalidRequest
            }

            is DataNotFoundException -> {
                AppLogger.d(
                    tag = "DataNotFound",
                    message = "Data not found for = $data"
                )
                GetReqDomainFailure.DataNotFound(throwable.message ?: "Data not found")
            }

            else -> {
                AppLogger.e(
                    tag = "UnknownFailure",
                    message = "Unexpected error in cancelBooking, data is $data",
                    t = throwable
                )
                GetReqDomainFailure.Unknown(throwable)
            }
        }
    }

    private fun <T> mapFirestoreException(
        exception: FirebaseFirestoreException,
        data: T
    ): GetReqDomainFailure {

        return when (exception.code) {

            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                AppLogger.e(
                    tag = "FirebasePermission",
                    message = "Permission denied for bookingId=$data",
                    t = exception
                )
                GetReqDomainFailure.PermissionDenied(exception.message ?: "Permission Denied.")
            }

            FirebaseFirestoreException.Code.NOT_FOUND -> {
                AppLogger.d(
                    tag = "DataNotFound",
                    message = "Data not found for = $data"
                )
                GetReqDomainFailure.DataNotFound(exception.message ?: "Data not found")
            }

            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                GetReqDomainFailure.NoInternet
            }

            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                // Mostly index missing
                GetReqDomainFailure.InvalidRequest
            }

            else -> {
                AppLogger.e(
                    tag = "UnknownFailure",
                    message = "Unexpected error in cancelBooking, data is $data",
                    t = exception
                )
                GetReqDomainFailure.Unknown(exception)
            }
        }
    }
}