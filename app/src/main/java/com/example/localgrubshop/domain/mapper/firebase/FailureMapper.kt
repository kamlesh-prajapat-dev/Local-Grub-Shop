package com.example.localgrubshop.domain.mapper.firebase

import com.example.localgrubshop.utils.AppLogger
import com.example.localgrubshop.utils.DataNotFoundException
import com.example.localgrubshop.utils.DataParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DatabaseException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.text.contains

fun <T>Throwable.toGetReqDomainFailure(data: T): GetReqDomainFailure {
    return when (this) {
        is SecurityException -> {
            AppLogger.e(
                tag = "FirebasePermission",
                message = "Permission denied for bookingId=$data",
                t = this
            )
            GetReqDomainFailure.PermissionDenied(this.message ?: "")
        }
        is IOException -> GetReqDomainFailure.Network
        is DataNotFoundException -> GetReqDomainFailure.DataNotFount(this.message ?: "The requested data was not found.")
        is DataParsingException -> {
            AppLogger.e(
                tag = "ObserveBookings",
                message = "Unexpected error while parsing data $data",
                t = this
            )
            GetReqDomainFailure.InvalidData(this.message ?: "")
        }
        else -> {
            AppLogger.e(
                tag = "UnknownFailure",
                message = "Unexpected error in cancelBooking, data is $data",
                t = this
            )
            GetReqDomainFailure.Unknown(this)
        }
    }
}

fun <T> Throwable.toWriteReqDomainFailure(data: T): WriteReqDomainFailure {
    return when (this) {
        is FirebaseNetworkException -> {
            WriteReqDomainFailure.NoInternet
        }

        is DatabaseException -> {
            if (this.message?.contains("Permission denied", true) == true) {
                AppLogger.e(
                    tag = "FirebasePermission",
                    message = "Permission denied for bookingId=$data",
                    t = this
                )
                WriteReqDomainFailure.PermissionDenied(this.message ?: "Permission denied")
            } else {
                WriteReqDomainFailure.Unknown(this)
            }
        }

        is DataNotFoundException -> {
            WriteReqDomainFailure.DataNotFound(this.message ?: "The requested data was not found.")
        }

        is IllegalArgumentException -> {
            WriteReqDomainFailure.ValidationError(this.message ?: "Invalid input")
        }

        is CancellationException -> {
            WriteReqDomainFailure.Cancelled(this.message ?: "Cancelled")
        }

        is IllegalStateException -> {
            AppLogger.e(
                tag = "FirebaseIllegalStateException",
                message = "${this.message} for user: $data",
                t = this
            )
            WriteReqDomainFailure.Unknown(this)
        }

        else -> {
            AppLogger.e(
                tag = "UnknownFailure",
                message = "Unexpected error in cancelBooking",
                t = this
            )
            WriteReqDomainFailure.Unknown(this)
        }
    }
}
