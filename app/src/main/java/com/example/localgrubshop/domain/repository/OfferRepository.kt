package com.example.localgrubshop.domain.repository

import com.example.localgrubshop.data.models.NewOffer
import com.example.localgrubshop.domain.models.result.OfferResult
import kotlinx.coroutines.flow.Flow

interface OfferRepository {
    fun getOffers(): Flow<OfferResult>
    suspend fun addOffer(offer: NewOffer): OfferResult
    suspend fun updateOffer(offer: NewOffer, id: String): OfferResult
    suspend fun deleteOffer(offerId: String): OfferResult
    suspend fun updateOfferStatus(offerId: String, offerStatus: String): OfferResult
}
