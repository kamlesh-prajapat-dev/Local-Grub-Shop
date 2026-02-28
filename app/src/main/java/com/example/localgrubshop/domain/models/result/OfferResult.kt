package com.example.localgrubshop.domain.models.result

import com.example.localgrubshop.data.models.GetOffer
import com.example.localgrubshop.data.models.NewOffer

sealed interface OfferResult {
    data class GetSuccess(val offers: List<GetOffer>) : OfferResult
    data class Failure(val failure: Exception): OfferResult
    data class AddSuccess(val newOffer: NewOffer, val id: String): OfferResult
    data class DeleteSuccess(val isSuccess: Boolean): OfferResult
    data class UpdateOfferStatusSuccess(val isSuccess: Boolean): OfferResult
}